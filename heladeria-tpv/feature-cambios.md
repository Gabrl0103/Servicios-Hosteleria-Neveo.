# Especificación: Cajeros al abrir turno, pantalla de Configuración, y mejoras de gráficos

## Contexto del proyecto

Monorepo `heladeria-tpv/` con tres carpetas:
- `backend/` — Spring Boot (Java 17), JPA + SQLite, paquete base `com.heladeria.tpv`
- `frontend/` — React + Vite, sin TypeScript, estilos inline + `src/styles/global.css`
- `electron/` — cascarón de escritorio (no se toca en este cambio)

No hay sistema de login. Existe un usuario de sistema fijo (`User` id=1) usado internamente para las relaciones de base de datos en `Order` y `CashRegister`, pero la persona que usa la app nunca ve ni elige ese usuario.

Patrones ya establecidos a seguir:
- Backend: capas `model/`, `repository/`, `service/`, `controller/`, `dto/`, con `GlobalExceptionHandler` ya manejando `ResourceNotFoundException` y `BusinessRuleException`.
- Frontend: cada pantalla es un archivo en `src/pages/`, funciones de API en `src/api/*.js` que usan el cliente axios de `src/api/client.js`. Estilos inline con variables CSS definidas en `src/styles/global.css` (paleta basada en `--ink`, `--accent`, `--surface`, etc — revisar ese archivo antes de escribir estilos nuevos). Íconos SVG vía `<use href="#nombre-icono">`, definidos como `<symbol>` en `src/components/IconDefs.jsx`.
- El menú principal vive en `src/components/NavBar.jsx`, con un array `ITEMS` que define las pestañas.

---

## Cambio 1: Cajeros (lista de nombres) y flujo de apertura de turno

### Por qué
Aunque no hay login, el dueño quiere saber qué persona (de un set fijo de nombres conocidos) atendió cada turno, sin necesidad de contraseñas. Hoy el nombre queda fijo como "Neveo" en el recibo; con este cambio se selecciona explícitamente al abrir el turno.

### Backend

**Entidad nueva** `Cashier` (`model/Cashier.java`):
- `id` (Long, autogenerado)
- `name` (String, not null)

**Repositorio**: `CashierRepository extends JpaRepository<Cashier, Long>` — CRUD estándar, sin queries custom.

**DTO** `CashierRequest`: campo `name` (String, `@NotBlank`).

**Servicio** `CashierService`:
- `findAll()` → `List<Cashier>`
- `create(CashierRequest)` → crea y devuelve
- `delete(Long id)` → elimina; si no existe, lanza `ResourceNotFoundException`

**Controlador** `CashierController` en `/api/cashiers`:
- `GET /api/cashiers` → lista todos
- `POST /api/cashiers` → crea
- `DELETE /api/cashiers/{id}` → elimina

**Modificar entidad `CashRegister`** (`model/CashRegister.java`):
- Agregar campo `cashierName` (String, not null) — el nombre del cajero elegido al abrir, guardado como texto plano (no FK a `Cashier`, porque si se elimina el cajero después, el historial de turnos pasados no debe romperse ni perder el dato).
- Constructor pasa a recibir `(User openedBy, String cashierName)`.
- Mantener el campo `openedBy` (User) tal cual está, sigue siendo el usuario de sistema interno.

**Modificar `CashRegisterService.openRegister`**:
- Firma cambia a `openRegister(Long userId, String cashierName)`.
- Si `cashierName` es null o vacío, lanzar `BusinessRuleException("Debes seleccionar un cajero")`.
- El resto de la lógica (validar que no haya turno ya abierto) se mantiene igual.

**Modificar `CashRegisterController.open`**:
- El endpoint `POST /api/cash-registers/open` ahora recibe también `cashierName` como query param o en un pequeño body — decisión de Claude Code, pero debe quedar consistente con cómo el frontend ya llama a `openCashRegister` hoy (revisar `frontend/src/api/cashRegisters.js`). Lo más simple: agregar un segundo `@RequestParam String cashierName`.

**Modificar `ShiftReceiptResponse` y el comprobante** (`ReportService.getShiftReceipt`, `ShiftReceiptPage.jsx`):
- El comprobante actualmente muestra "RESPONSABLE: Neveo" fijo (texto hardcodeado en `ShiftReceiptPage.jsx`). Debe mostrar `cashRegister.cashierName` en su lugar.
- Agregar `cashierName` a `ShiftReceiptResponse` (nuevo campo, viene de `CashRegister.getCashierName()`).

**Modificar `CashBoxHistoryPage.jsx`**: la columna "Responsable" debe mostrar el `cashierName` real de cada turno (hoy dice "Neveo" fijo) — necesita que el endpoint `GET /api/cash-registers` (que ya existe y devuelve `List<CashRegister>`) incluya ese campo, lo cual ocurre automáticamente al agregarlo a la entidad.

### Frontend

**Nuevo archivo `src/api/cashiers.js`**:
```js
getCashiers() // GET /api/cashiers
createCashier(name) // POST /api/cashiers
deleteCashier(id) // DELETE /api/cashiers/{id}
```

**Modificar `src/api/cashRegisters.js`**: `openCashRegister(userId, cashierName)` debe mandar ambos parámetros al backend.

**Modificar `src/pages/CashRegisterPage.jsx`** — este es el cambio de flujo más visible:

Hoy, al hacer click en "Abrir turno", se llama `handleOpen()` directamente y abre el turno de inmediato. Esto debe cambiar a:

1. Click en "Abrir turno" → en vez de abrir directo, despliega un panel/card (inline en la misma pantalla, no modal aparte — usar el mismo patrón visual de tarjeta que ya tiene la pantalla) con:
   - Un selector (lista de botones o un `<select>` estilizado, decisión de Claude Code según lo que se vea mejor con los cajeros cargados de `getCashiers()`) para elegir el cajero.
   - Un **switch/interruptor** (toggle on-off, estilo iOS — círculo que se desliza dentro de una píldora, ya hay precedente de este patrón visual en `QuantityModal` y otros componentes de la app, usar colores `--ink` para activo y `--border` para inactivo) que el usuario debe activar para confirmar.
   - El turno solo se abre cuando: hay un cajero seleccionado Y el switch está activado. Mientras falte alguna de las dos condiciones, el botón de confirmar apertura debe estar deshabilitado.
2. Si `getCashiers()` devuelve una lista vacía, no mostrar el selector — en su lugar mostrar un mensaje ("No hay cajeros configurados") con un botón/link que navegue a `/configuracion` (la pantalla nueva del Cambio 2).

### Criterio de terminado (Cambio 1)
- Se puede crear y eliminar cajeros desde la pantalla de Configuración (ver Cambio 2).
- Al ir a abrir turno sin cajeros creados, se bloquea con mensaje y enlace a Configuración.
- Con cajeros creados, abrir turno exige elegir uno + activar el switch antes de habilitar la confirmación.
- El nombre elegido aparece en: el recibo de cierre (`/recibo/:id`) reemplazando "Neveo" fijo, y en la columna "Responsable" de Cuadre de caja.

---

## Cambio 2: Pantalla de Configuración

### Por qué
Centralizar datos editables del negocio (hoy hardcodeados en el recibo), la gestión de cajeros del Cambio 1, y un backup manual de la base de datos — sin tocar la pantalla de Productos, que se queda donde está.

### Backend

**Entidad nueva** `BusinessSettings` (`model/BusinessSettings.java`):
- `id` (Long, fijo en `1L`, no autogenerado — es una fila única)
- `businessName` (String, not null)
- `nit` (String, nullable)
- `address` (String, nullable)
- `phone` (String, nullable)

**Repositorio**: `BusinessSettingsRepository extends JpaRepository<BusinessSettings, Long>`.

**DTO** `BusinessSettingsRequest`: `businessName` (`@NotBlank`), `nit`, `address`, `phone`.

**Servicio** `BusinessSettingsService`:
- `get()` → devuelve la fila id=1; si no existe, la crea con valores de ejemplo iguales a los que hoy están hardcodeados en `ShiftReceiptPage.jsx` ("Heladeria", "901.234.567-8", "Calle 12 # 34-56, Bogotá", "310 123 4567") y la guarda.
- `update(BusinessSettingsRequest)` → actualiza la fila id=1 (debe existir ya por el `get()`, o crearla si no).

**Controlador** `BusinessSettingsController` en `/api/business-settings`:
- `GET /api/business-settings` → `get()`
- `PUT /api/business-settings` → `update(request)`

**Modificar `ReportService.getShiftReceipt` y/o `ShiftReceiptResponse`**: agregar los 4 campos de `BusinessSettings` a la respuesta del comprobante (businessName, nit, address, phone), para que `ShiftReceiptPage.jsx` ya no use los valores hardcodeados sino estos.

**Backup de base de datos** — nuevo endpoint para descargar el archivo `.db`:
- Controlador nuevo `BackupController` en `/api/backup`:
  - `GET /api/backup/database` → debe devolver el archivo `heladeria.db` como descarga binaria (`Content-Disposition: attachment; filename="heladeria-backup-{fecha}.db"`, `Content-Type: application/octet-stream`).
  - Para ubicar el archivo: leer la misma property que usa `application.properties` para la ruta de SQLite (`spring.datasource.url=jdbc:sqlite:${APP_DATA_DIR:.}/heladeria.db` — revisar el valor real de `APP_DATA_DIR` vía `@Value` o `Environment` para construir la ruta absoluta del archivo a servir).
  - Usar `ResponseEntity<Resource>` con `FileSystemResource`, patrón estándar de Spring para servir archivos.

### Frontend

**Nuevo archivo `src/api/businessSettings.js`**:
```js
getBusinessSettings() // GET /api/business-settings
updateBusinessSettings(data) // PUT /api/business-settings
```

**Nuevo archivo `src/api/backup.js`**:
```js
downloadBackup() // GET /api/backup/database — debe disparar la descarga del navegador.
// Patrón sugerido: usar window.location.href = `${baseURL}/backup/database` o fetch + blob + link temporal con click(). Decisión de Claude Code, lo importante es que el navegador descargue el archivo sin abrir una pestaña en blanco permanente.
```

**Nueva pantalla `src/pages/SettingsPage.jsx`**, con tres secciones dentro de una sola pantalla (cards apiladas o en grid, seguir el estilo visual ya usado en `ProductsPage.jsx` y `CashRegisterPage.jsx` — fondo `var(--bg)`, cards con `var(--surface)`, bordes `var(--border)`):

1. **Datos del negocio**: formulario con los 4 campos (nombre, NIT, dirección, teléfono), botón "Guardar cambios". Al guardar, llama `updateBusinessSettings` y muestra confirmación visual breve (ej: mensaje verde que desaparece a los 2-3 segundos, o deshabilitar el botón brevemente con texto "Guardado").

2. **Cajeros**: lista de cajeros existentes (nombre + botón eliminar con ícono, mismo patrón que usa `ProductsPage.jsx` para sus botones de acción), input + botón "Agregar cajero" para crear uno nuevo. Confirmar con `window.confirm` antes de eliminar (mismo patrón ya usado en `TablesPage.jsx` para eliminar mesas).

3. **Respaldo de datos**: texto explicativo corto ("Descarga una copia de seguridad de toda la información del sistema") + botón "Descargar copia de seguridad" que llama `downloadBackup()`.

**Modificar `src/components/NavBar.jsx`**: agregar a `ITEMS` una entrada nueva `{ to: '/configuracion', label: 'Configuración', icon: 'ic-settings' }`, ubicada al final de la lista (después de Productos).

**Modificar `src/components/IconDefs.jsx`**: agregar un nuevo `<symbol id="ic-settings">` — ícono simple de engranaje/ajustes, mismo estilo de trazo que los demás íconos del archivo (`stroke="currentColor"`, `strokeWidth="2"`, sin relleno).

**Modificar `src/App.jsx`**: agregar la ruta `<Route path="/configuracion" element={<Layout><SettingsPage /></Layout>} />`.

**Modificar `src/pages/ShiftReceiptPage.jsx`**: reemplazar los datos hardcodeados del negocio (HELADERIA, NIT 901.234.567-8, etc.) por los campos que ahora vienen en la respuesta de `getShiftReceipt` (ver cambio de backend arriba). Si la respuesta no trae esos campos por alguna razón, usar los mismos valores de ejemplo como fallback.

### Criterio de terminado (Cambio 2)
- Pantalla `/configuracion` accesible desde el menú, con las 3 secciones funcionando.
- Cambiar el nombre del negocio en Configuración y luego abrir un recibo (`/recibo/:id`) refleja el nuevo nombre.
- Agregar/eliminar cajeros en Configuración se refleja inmediatamente en el selector de "Abrir turno".
- El botón de backup descarga un archivo `.db` válido (se puede verificar que el archivo descargado abre correctamente con cualquier visor de SQLite).

---

## Cambio 3: Mejoras a los gráficos de Reportes (eje numérico + tooltip)

### Por qué
Referencia visual: capturas de Loggro Restobar muestran, en su gráfico de barras, valores numéricos de referencia en el eje vertical (ej: 0 — 5.000.000 — 10.000.000 — 15.000.000 — 20.000.000), alineados a líneas horizontales de la cuadrícula. Hoy los gráficos de `ReportsPage.jsx` (tanto el de línea como el de barras) solo muestran las formas (línea/barras) sin ningún número de referencia en el eje — son difíciles de interpretar a simple vista.

### Alcance
Afecta **ambos** gráficos ya existentes en `src/pages/ReportsPage.jsx`:
1. El gráfico de línea (`buildChartGeometry`, ventas por día del rango seleccionado).
2. El gráfico de barras (`buildBarGeometry`, facturado por mes, últimos 6 meses).

### Comportamiento esperado

**Eje vertical con valores de referencia**:
- Calcular automáticamente 4-5 líneas de referencia horizontales según el valor máximo de los datos mostrados (no valores fijos como en el ejemplo de Loggro, sino proporcionales a los datos reales — ej: si el máximo es 350.000, las líneas podrían ser 0, 87.500, 175.000, 262.500, 350.000, redondeadas a un número "limpio" como 0, 100.000, 200.000, 300.000, 350.000).
- Cada línea horizontal de la cuadrícula debe tener su valor numérico escrito a la izquierda del gráfico, en formato compacto (usar la función `formatCurrency` ya existente en `src/utils/format.js`, o una versión abreviada si los números son muy largos — decisión de Claude Code, pero debe ser legible, ej: "$350.000" o "$350k", consistente entre todas las etiquetas del mismo gráfico).
- Esto requiere ampliar el `viewBox`/padding izquierdo del SVG en ambas funciones de geometría (`buildChartGeometry` y `buildBarGeometry`) para dejar espacio a esas etiquetas sin que se corten ni se sobrepongan con las barras/línea.

**Tooltip al pasar el mouse (hover)**:
- Gráfico de barras: al pasar el mouse sobre una barra, debe aparecer un tooltip mostrando el mes (label) y el valor exacto formateado como moneda (ej: "Junio: $450.000"). El tooltip puede ser un `<title>` SVG nativo (más simple) o un elemento posicionado con React state (más control visual, consistente con el resto de la app que usa tarjetas con sombra `var(--shadow-card)` o similar) — preferir la segunda opción si no añade complejidad desproporcionada, ya que se ve más profesional y es coherente con el resto del diseño del sistema.
- Gráfico de línea: mismo comportamiento al pasar el mouse sobre cada punto/círculo de la línea — mostrar fecha exacta + valor.
- En ambos casos, al pasar el mouse sobre el elemento (barra o punto), también se recomienda resaltarlo visualmente (ej: aumentar opacidad o agregar un borde/glow sutil) para reforzar qué elemento corresponde al tooltip mostrado.

### Criterio de terminado (Cambio 3)
- Ambos gráficos muestran valores numéricos en el eje vertical, alineados a líneas de cuadrícula, legibles y proporcionales a los datos reales de cada carga.
- Pasar el mouse sobre cualquier barra o punto de línea muestra el valor exacto correspondiente.
- No se rompe ninguna funcionalidad existente de `ReportsPage.jsx` (KPIs fijos, selector de rango, ranking de productos, desglose por método de pago siguen funcionando igual).

---

## Notas generales para Claude Code

- Seguir el estilo de código ya presente en el proyecto (nombres de variables, estructura de carpetas, convenciones de Spring Boot con constructor injection, DTOs simples con getters/setters explícitos en vez de records, dado que así está el resto del proyecto).
- No introducir librerías nuevas de gráficos (como Chart.js o Recharts) para el Cambio 3 — los gráficos actuales son SVG hechos a mano dentro del componente; las mejoras deben mantenerse en ese mismo enfoque para no añadir una dependencia nueva solo por esto.
- Después de implementar, borrar `backend/heladeria.db` si existe (cambios de esquema: nuevas tablas `cashiers`, `business_settings`, nueva columna `cashier_name` en `cash_registers`) y verificar que el backend arranca limpio con `mvn spring-boot:run`, y que el frontend compila con `npm run build` sin errores antes de dar por terminado.