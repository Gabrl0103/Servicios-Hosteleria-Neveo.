# Especificación 2: Recibo individual, caja inicial, monto manual, mesas arrastrables, configuración ampliada

## Contexto

Continuación de `2026-06-20-cajeros-config-graficos.md` (asumir que ya está implementado: existen `Cashier`, `BusinessSettings`, `BusinessSettingsController`, `SettingsPage.jsx`, el flujo de apertura de turno con selector de cajero + switch, y las mejoras de gráficos). Mismo stack y convenciones ya usadas (ver ese documento para detalles de estilo si hace falta repasar).

---

## Cambio 1: Recibo de confirmación por cada venta individual

### Por qué
Hoy solo existe un recibo en formato ticket térmico para el **cierre de turno completo** (`/recibo/:cashRegisterId`, componente `ShiftReceiptPage.jsx`). Falta el equivalente por cada venta individual — cuando se cobra un pedido (desde `TableDetailPage.jsx`, función `handleConfirmPayment`), no se genera ningún comprobante visual de esa transacción puntual.

### Backend
No requiere cambios de modelo: la entidad `Order` ya tiene todo lo necesario (`items`, `total`, `discountPercent`, `paymentMethod`, `amountReceived`, `changeGiven`, `createdAt`). Verificar que el endpoint existente `GET /api/orders/{id}` (en `OrderController`, ya existente) devuelva un `OrderResponse` completo con la lista de items — si ya lo hace, no se necesita ningún endpoint nuevo.

Si `OrderResponse` no incluye actualmente los datos del negocio (nombre, NIT, etc.) para imprimir el encabezado del ticket, **no los agregues ahí** — el frontend ya tiene `getBusinessSettings()` disponible (del cambio anterior) y debe consultarlo por separado, igual que hace `ShiftReceiptPage.jsx`.

### Frontend

**Nueva página `src/pages/OrderReceiptPage.jsx`**, ruta independiente sin `NavBar` (mismo patrón que `ShiftReceiptPage.jsx`: fondo gris `#e7e0d3`, ticket centrado de 300px de ancho, fuente `mono`, bordes dentados arriba/abajo, botones "Cerrar ventana" / "Imprimir").

Contenido del ticket (formato calcado al de `ShiftReceiptPage.jsx` en cuanto a estilo, pero con estos datos):
- Encabezado: nombre del negocio, NIT, dirección, teléfono (desde `getBusinessSettings()`), igual que ya se hace en `ShiftReceiptPage.jsx`.
- Línea separadora punteada.
- "COMPROBANTE DE VENTA" + número de orden (`N° {id padded a 6 dígitos}`, mismo `padStart` que se usa para el de turno).
- Fecha y hora de la venta (`order.createdAt`).
- Nombre de la mesa si existe (`order.tableName`).
- Lista de items: cada uno con cantidad, nombre del producto, y subtotal (formato: `2x Acai 9oz ............ $30.000`, alineado con `display:flex; justify-content:space-between` como ya se hace en otras tablas de la app). Si el item tiene `note`, mostrarla en una línea pequeña debajo, en cursiva o con un prefijo como `> sin azúcar`.
- Si `order.discountPercent` no es null, mostrar una línea "Descuento {X}%: -$monto" antes del total.
- Total final destacado (mismo estilo que el total del ticket de turno: `fontSize: 15, fontWeight: 700`).
- Método de pago usado, y si fue efectivo, mostrar "Recibido" y "Cambio" cuando esos datos existen (`order.amountReceived`, `order.changeGiven`).
- Pie: "*** GRACIAS POR SU COMPRA ***" en lugar del texto legal del recibo de turno.

**Nuevo archivo de ruta en `src/App.jsx`**: agregar `<Route path="/recibo-venta/:orderId" element={<OrderReceiptPage />} />` (fuera del `Layout`, igual que la ruta de recibo de turno).

**Modificar `src/pages/TableDetailPage.jsx`**: en `handleConfirmPayment`, después de que `createOrder(...)` responde exitosamente, abrir automáticamente el recibo de esa venta con `window.open(`/recibo-venta/${order.id}`, '_blank', 'width=420,height=720')` (mismo patrón que ya usa `CashBoxHistoryPage.jsx` para el recibo de turno). El `id` viene en la respuesta de `createOrder` (`OrderResponse.id`).

### Criterio de terminado
- Al cobrar cualquier pedido desde una mesa, se abre automáticamente una ventana/pestaña nueva mostrando el ticket de esa venta específica, con sus items, descuento si aplica, y método de pago.
- El ticket se puede imprimir con el botón correspondiente.

---

## Cambio 2: Captura de valor inicial de caja (sí afecta cálculos)

### Por qué
Inicialmente el sistema decidió NO manejar monto inicial de caja. Ahora se revierte esa decisión: el monto inicial sí se captura al abrir turno y sí debe sumarse al "esperado en caja" junto con las ventas en efectivo — exactamente la misma fórmula que se había descartado antes (`esperado = monto_inicial + ventas_efectivo_del_turno`). Nequi y Rappi NO se suman a ese cálculo porque ese dinero no está físicamente en el cajón.

### Backend

**Modificar entidad `CashRegister`**: agregar campo `openingAmount` (BigDecimal, precision 12 scale 2, **nullable** — turnos antiguos ya existentes no lo tendrán, debe tratarse como `0` en esos casos al calcular).

**Modificar `CashRegisterService.openRegister`**: la firma pasa a `openRegister(Long userId, String cashierName, BigDecimal openingAmount)`. Si `openingAmount` es null, tratarlo como `BigDecimal.ZERO` (no lanzar error — el valor inicial puede quedar en cero si el usuario no escribe nada, ver UX abajo).

**Modificar `CashRegisterController.open`**: agregar el parámetro `openingAmount` (BigDecimal, opcional, `@RequestParam(required = false)`).

**Nuevo cálculo "esperado en caja"**: agregar un método en `ReportService` (o donde tenga más sentido según lo que ya exista) que devuelva, para un `cashRegisterId` dado:
```
expectedCash = (cashRegister.openingAmount ?? 0) + sumaTotalDeOrdenesConfirmadas(cashRegisterId, paymentMethod=EFECTIVO)
```
Expón esto en un endpoint nuevo, por ejemplo `GET /api/reports/shift/{cashRegisterId}/expected-cash`, que devuelva un objeto simple `{ openingAmount, cashSales, expectedCash }`.

**Modificar `ShiftReceiptResponse`** (el comprobante): agregar los 3 campos anteriores (`openingAmount`, `cashSales`, `expectedCash`) para que el ticket de cierre pueda mostrarlos.

### Frontend

**Modificar el flujo de apertura de turno** (`CashRegisterPage.jsx`, ya construido en el cambio anterior con selector de cajero + switch): agregar al mismo panel desplegable un campo numérico más, **"Valor inicial de caja"** (input tipo number, placeholder "$0", opcional — si se deja vacío, se manda como `0` o `null`, decisión de Claude Code pero debe ser consistente con el backend). Este campo NO debe bloquear la apertura si se deja en blanco (a diferencia del cajero, que sí es obligatorio).

**Modificar `src/api/cashRegisters.js`**: `openCashRegister(userId, cashierName, openingAmount)` debe mandar los 3 parámetros.

**Modificar `CashRegisterPage.jsx` (vista de turno ya abierto)**: agregar una tarjeta/dato más junto al resumen por método de pago existente, mostrando "Esperado en caja: $X" (usando el nuevo endpoint de `expected-cash`), para que se pueda comparar contra el conteo físico real.

**Modificar `ShiftReceiptPage.jsx`**: agregar al ticket, después del desglose por método de pago, una sección con:
```
CAJA INICIAL: $X
VENTAS EFECTIVO: $Y
ESPERADO EN CAJA: $Z
```
(mismo estilo de filas `Row` ya usado en ese componente).

### Criterio de terminado
- Al abrir turno, se puede escribir un valor inicial (opcional).
- Mientras el turno está abierto, se ve el "esperado en caja" actualizado considerando ese valor inicial + ventas en efectivo.
- El recibo de cierre de turno muestra el desglose de caja inicial / ventas efectivo / esperado en caja.
- Turnos abiertos antes de este cambio (sin `openingAmount`) no rompen ningún cálculo (se tratan como si el valor inicial fuera 0).

---

## Cambio 3: Campo de monto manual en el cobro (excluyente con los botones de billete)

### Por qué
Hoy `PaymentModal.jsx` solo permite construir el "monto recibido" tocando botones de billete (10.000/20.000/50.000/100.000) que se van sumando. No existe ningún campo de texto donde escribir un número directamente con el teclado (por ejemplo, si el cliente paga con una combinación que no calza con los botones disponibles).

### Frontend únicamente (no requiere cambios de backend — el campo `amountReceived` que ya se envía sigue siendo el mismo).

**Modificar `src/components/PaymentModal.jsx`**:
- Agregar un input numérico nuevo, **separado y visualmente distinto** de la casilla "RECIBIDO" actual (que sigue siendo de solo lectura, alimentada únicamente por los botones de billete). Sugerencia de ubicación: debajo de los botones de billete, con su propia etiqueta, por ejemplo "O escribe el monto manualmente".
- Comportamiento: son alternativas **excluyentes**, no acumulables.
  - Si el usuario escribe algo en el campo manual, ese valor se vuelve el `received` efectivo (el que se usa para calcular el cambio y el que se envía como `amountReceived` al confirmar), y cualquier acumulado previo de los botones de billete queda descartado/ignorado.
  - Si el usuario vuelve a tocar un botón de billete después de haber escrito en el campo manual, el campo manual debe limpiarse y el comportamiento de suma por botones vuelve a tomar el control (es decir, la última acción —botón o campo manual— "gana" y resetea a la otra vía).
- El campo "RECIBIDO" de solo lectura debe reflejar el valor activo sin importar de cuál de las dos vías vino (botones o campo manual), para que el resto de la UI (cálculo de cambio, validación de monto suficiente) no necesite duplicar lógica.

### Criterio de terminado
- Existe un campo de texto visible y diferenciado del recuadro "RECIBIDO" actual, donde se puede escribir un número con el teclado.
- Escribir ahí actualiza el cálculo de cambio igual que lo haría tocar botones de billete.
- Usar el campo manual descarta lo acumulado por botones, y viceversa — nunca se suman ambas fuentes a la vez.

---

## Cambio 4: Mesas con posición libre (arrastrables)

### Por qué
Referencia: foto de Loggro adjunta en el proyecto (mesas nombradas como "marketing", "ERIKA", "PATTY", "SUSANA", "Manu", "Pipe", distribuidas libremente por la pantalla, no en una grilla ordenada). Hoy `TablesPage.jsx` muestra las mesas en un grid CSS automático (`grid-template-columns: repeat(auto-fill, minmax(180px, 1fr))`), sin control de posición individual.

### Backend

**Modificar entidad `RestaurantTable`**: agregar dos campos, `positionX` (Integer o Double, nullable) y `positionY` (Integer o Double, nullable) — coordenadas en píxeles relativas al lienzo de la pantalla de mesas. Si son null (mesas creadas antes de este cambio, o recién creadas), el frontend debe asignarles una posición por defecto (ver abajo).

**Modificar `RestaurantTableRequest`** o crear un DTO nuevo `UpdateTablePositionRequest` con `positionX` y `positionY` — decisión de Claude Code sobre si reutilizar el request existente (agregándole estos campos como opcionales) o crear uno dedicado solo para la actualización de posición.

**Nuevo endpoint** en `RestaurantTableController`: `PATCH /api/tables/{id}/position` que recibe `{ positionX, positionY }` y actualiza solo esos dos campos (sin tocar nombre ni pendingTotal). Usar un servicio dedicado o un método nuevo en `RestaurantTableService`.

### Frontend

**Modificar `src/pages/TablesPage.jsx`**:
- Cambiar el contenedor de mesas de un grid CSS a un área de posicionamiento libre: `position: relative` en el contenedor, con altura fija generosa (ej: `600px` o el alto disponible de la pantalla), y cada tarjeta de mesa con `position: absolute; left: {positionX}px; top: {positionY}px`.
- Si una mesa no tiene posición guardada (`positionX`/`positionY` null), calcular una posición inicial automática (por ejemplo, distribuir en una grilla simple la primera vez, igual que el comportamiento actual) y guardarla inmediatamente vía el nuevo endpoint, para que la próxima vez ya tenga una posición persistida.
- Implementar arrastre con eventos nativos de mouse (`onMouseDown`, `onMouseMove`, `onMouseUp` en el contenedor padre, actualizando un estado de "mesa siendo arrastrada" + posición temporal) — no es necesario instalar una librería de drag-and-drop, el área de arrastre es simple. Al soltar (`onMouseUp`), llamar al endpoint de posición para persistir.
- **Límites**: la posición no debe permitir que la tarjeta salga del área visible del contenedor — al calcular la nueva posición durante el arrastre, usar `Math.max(0, Math.min(posicion, anchoContenedor - anchoTarjeta))` tanto para X como para Y.
- El click normal (sin arrastrar) debe seguir navegando a `/mesas/:id` como hoy — diferenciar un click de un drag por la distancia recorrida del mouse (si el movimiento total fue menor a unos ~5px, tratarlo como click; si fue mayor, tratarlo como arrastre y no navegar al soltar).
- Los botones de editar/eliminar que ya existen sobre cada tarjeta (esquina superior derecha) deben seguir funcionando con click directo sin iniciar arrastre — usar `e.stopPropagation()` ahí como ya se hace.

### Criterio de terminado
- Cada mesa se puede arrastrar con el mouse a cualquier posición dentro del área de la pantalla.
- La posición se mantiene al recargar la página o reabrir la app (persistida en backend).
- No se puede arrastrar una mesa fuera del área visible.
- El click simple (sin arrastrar) sigue abriendo el detalle de la mesa con normalidad.

---

## Cambio 5: Configuración ampliada — logo del negocio y datos adicionales

### Por qué
Se busca que la pantalla de Configuración (ya creada en el cambio anterior con datos del negocio, cajeros, y backup) se sienta más completa y profesional, agregando un logo visual y datos adicionales del negocio.

### Backend

**Modificar entidad `BusinessSettings`**: agregar campos:
- `logoBase64` (tipo `TEXT` o `@Lob` en la columna — debe poder almacenar un string largo; usar `@Column(columnDefinition = "TEXT")`), nullable. Contiene la imagen codificada en base64 (incluir el prefijo `data:image/...;base64,` o guardarlo limpio y reconstruirlo en el frontend — decisión de Claude Code, pero debe ser consistente entre guardar y leer).
- `businessHours` (String, nullable) — texto libre, ej: "Lun-Sáb 10am-8pm".
- `instagramHandle` (String, nullable).
- `facebookUrl` (String, nullable).
- `whatsappNumber` (String, nullable).

**Modificar `BusinessSettingsRequest`**: agregar los mismos 5 campos.

**Sin endpoint nuevo** — el `PUT /api/business-settings` ya existente debe aceptar estos campos adicionales en el mismo request. Verificar que el tamaño máximo de request en Spring Boot (`server.tomcat.max-http-form-post-size` o similar en `application.properties`) sea suficiente para una imagen en base64 (recomendable subir el límite a algo como 5MB si no está ya configurado, ya que el default de Spring Boot puede ser muy bajo para esto).

### Frontend

**Modificar `src/pages/SettingsPage.jsx`**:
- En la sección "Datos del negocio" ya existente, agregar:
  - **Selector de logo**: un `<input type="file" accept="image/*">` oculto, activado por un botón/área clickeable que muestra una vista previa circular o cuadrada del logo actual (o un ícono placeholder si no hay logo). Al seleccionar un archivo, convertirlo a base64 con `FileReader` (`readAsDataURL`) antes de guardarlo en el estado del formulario. Validar que el archivo sea una imagen y que no sea excesivamente grande (sugerencia: límite de ~1-2MB antes de convertir, mostrando un error si se excede).
  - Campos nuevos de texto: "Horario de atención", "Instagram", "Facebook", "WhatsApp" — mismo estilo de input que los campos ya existentes (nombre, NIT, dirección, teléfono).
- Guardar todo junto con el mismo botón "Guardar cambios" ya existente, incluyendo el logo en base64 dentro del mismo payload de `updateBusinessSettings`.

**Modificar `src/components/NavBar.jsx`**: si `businessSettings.logoBase64` existe, mostrarlo como imagen pequeña (reemplazando o acompañando el cuadro con la letra "N" que hoy se usa como logo placeholder) en el header. Esto requiere que `NavBar.jsx` consulte `getBusinessSettings()` (puede cachearse en el `SessionContext` si ya existe ese patrón, o consultarse directo ahí con un `useEffect` — decisión de Claude Code según lo que ya esté establecido en el proyecto).

**Modificar `ShiftReceiptPage.jsx` y `OrderReceiptPage.jsx` (del Cambio 1)**: si `businessSettings.logoBase64` existe, mostrarlo como una imagen pequeña centrada arriba del nombre del negocio en el ticket (reemplazando el cuadro negro con "N" que se usa hoy como placeholder).

### Criterio de terminado
- Se puede subir una imagen como logo desde Configuración, con vista previa antes de guardar.
- El logo guardado aparece en el header de la app (NavBar) y en ambos tipos de recibo (turno y venta individual).
- Los campos de horario y redes sociales se guardan y persisten correctamente al recargar la pantalla de Configuración.

---

## Notas generales para Claude Code

- Mismas convenciones de estilo ya usadas en el proyecto (ver el primer documento de especificación si hace falta repasar).
- Para el Cambio 4 (mesas arrastrables), evitar instalar librerías de drag-and-drop (`react-dnd`, `@dnd-kit`, etc.) — el caso de uso es simple (arrastre libre dentro de un área, sin reordenar listas ni soltar en zonas específicas) y se puede resolver con eventos nativos de mouse sin dependencias nuevas.
- Para el Cambio 5, si el tamaño de la imagen en base64 genera problemas de rendimiento perceptible en el campo `TEXT` de SQLite, está bien — SQLite maneja columnas `TEXT` largas sin problema para este volumen de uso (una sola fila de configuración).
- Después de implementar todo, borrar `backend/heladeria.db` (cambios de esquema: nuevas columnas en `cash_registers`, `restaurant_tables`, `business_settings`) y verificar que `mvn spring-boot:run` y `npm run build` corran limpio antes de considerar terminado.
- Si alguno de los 5 cambios depende de que el primer documento de especificación ya esté aplicado (cajeros, switch de apertura, pantalla de Configuración base) y no lo está, avisar antes de continuar en vez de improvisar una versión distinta de esas piezas base.