# Especificación 3: Gastos del turno para cuadre de caja real

## Contexto

Continuación de los specs anteriores (`2026-06-20-cajeros-config-graficos.md` y `2026-06-21-recibo-caja-inicial-mesas-config.md`). Asumir que ya existen: `Cashier`, `BusinessSettings`, el flujo de apertura de turno con cajero + switch + valor inicial de caja, y el cálculo de "esperado en caja" (`openingAmount + ventasEfectivoDelTurno`) ya implementado en `ReportService` y mostrado en `CashRegisterPage.jsx` y en el ticket de `ShiftReceiptPage.jsx`.

Mismas convenciones de estilo y estructura del proyecto ya usadas (revisar specs anteriores si hace falta repasar patrones).

---

## Por qué

El cuadre de caja real del negocio no es solo "caja inicial + ventas en efectivo": durante el turno se hacen gastos en efectivo (ej: comprar insumos como leche), que salen físicamente del cajón antes del cierre. Hoy el sistema no contempla esto, así que "esperado en caja" no refleja la realidad — el dueño necesita poder registrar esos gastos durante el turno para que el cálculo final sea el correcto: `esperado en caja = caja inicial + ventas en efectivo - gastos del turno`.

---

## Backend

### Entidad nueva `Expense` (`model/Expense.java`)
- `id` (Long, autogenerado)
- `cashRegister` (`@ManyToOne`, `@JoinColumn(name = "cash_register_id")`, not null) — el gasto siempre pertenece a un turno específico.
- `description` (String, not null) — ej: "Leche Six Pack x3".
- `amount` (BigDecimal, precision 12 scale 2, not null).
- `createdAt` (LocalDateTime, not null, se asigna automáticamente al crear).

### Repositorio ExpenseRepository
- extends JpaRepository<Expense, Long>
- Método findByCashRegisterId(Long cashRegisterId) devuelve List<Expense>, para listar los gastos de un turno especifico.

### DTO ExpenseRequest
- cashRegisterId (Long, NotNull)
- description (String, NotBlank)
- amount (BigDecimal, NotNull, Positive)

### DTO ExpenseResponse
- id, description, amount, createdAt

### Servicio ExpenseService
- findByCashRegister(Long cashRegisterId): lista los gastos de ese turno.
- create(ExpenseRequest request): busca el CashRegister por cashRegisterId (si no existe, ResourceNotFoundException), crea y guarda el Expense con createdAt = LocalDateTime.now().
- delete(Long expenseId): si no existe, ResourceNotFoundException. Util para poder corregir un gasto mal ingresado antes de cerrar el turno.

### Controlador ExpenseController en /api/expenses
- GET /api/expenses?cashRegisterId={id} devuelve findByCashRegister(id)
- POST /api/expenses crea con create(request)
- DELETE /api/expenses/{id} elimina con delete(id)

### Modificar el cálculo de "esperado en caja" (en ReportService, donde ya vive la lógica de openingAmount + ventasEfectivo)
Nueva fórmula:

totalExpenses = suma de amount de todos los Expense de ese cashRegisterId
expectedCash = (cashRegister.openingAmount ?? 0) + cashSales - totalExpenses

Modificar el endpoint que ya existe (GET /api/reports/shift/{cashRegisterId}/expected-cash del spec anterior, o el nombre real que Claude Code le haya puesto) para incluir también totalExpenses en la respuesta, junto a openingAmount, cashSales, y el expectedCash ya recalculado con la resta.

### Modificar ShiftReceiptResponse (el comprobante de cierre)
Agregar totalExpenses y la lista de gastos (List<ExpenseResponse> o una versión simplificada solo con descripción + monto) para que el ticket de cierre pueda imprimir el detalle de gastos del turno, no solo el total.

---

## Frontend

### Nuevo archivo src/api/expenses.js
- getExpenses(cashRegisterId) -> GET /api/expenses?cashRegisterId=X
- createExpense(cashRegisterId, description, amount) -> POST /api/expenses
- deleteExpense(id) -> DELETE /api/expenses/{id}

### Modificar src/pages/CashRegisterPage.jsx (vista de turno abierto)
Agregar una nueva sección, "Gastos del turno", ubicada junto a (o debajo de) la tarjeta de "Esperado en caja" que ya existe del spec anterior. Debe incluir:

1. Formulario rápido para agregar un gasto: dos campos en línea (descripción texto, monto número) más un botón "Agregar gasto". Mismo estilo de inputs ya usado en otras pantallas (ProductsPage.jsx, TablesPage.jsx para sus formularios inline).
2. Lista de gastos ya registrados en este turno: cada uno mostrando descripción + monto + un botón pequeño de eliminar (ícono de basura, mismo patrón ya usado en ProductsPage.jsx).
3. Total de gastos destacado al final de la lista (suma simple, calcular en el frontend a partir de la lista ya cargada).

Al agregar o eliminar un gasto, debe refrescarse también la tarjeta de "Esperado en caja" (volviendo a pedir ese endpoint, ya que ahora la fórmula incluye los gastos).

### Modificar src/pages/ShiftReceiptPage.jsx (ticket de cierre de turno)
Agregar, después de la sección de caja inicial / ventas efectivo / esperado en caja (del spec anterior), una nueva sección en el ticket:

GASTOS DEL TURNO
Leche Six Pack x3 ........ $25.000
Bolsas .................... $5.000
TOTAL GASTOS .............. $30.000

Mismo estilo de filas (Row) ya usado en ese componente para las demás secciones. Si no hay gastos registrados en el turno, omitir esta sección completa (no mostrar "TOTAL GASTOS: $0" vacío sin sentido).

Y actualizar la línea de "ESPERADO EN CAJA" para que refleje la resta de gastos (ya viene calculado así desde el backend si se siguió la modificación de arriba).

---

## Criterio de terminado

- Durante un turno abierto, se puede agregar un gasto con descripción y monto desde CashRegisterPage.jsx.
- Los gastos agregados aparecen en una lista, se pueden eliminar individualmente.
- La tarjeta de "Esperado en caja" se actualiza automáticamente restando el total de gastos registrados.
- El ticket de cierre de turno (/recibo/:id) muestra el detalle de gastos (si los hay) y el "esperado en caja" final ya con la resta aplicada.
- Turnos que no tienen ningún gasto registrado siguen funcionando exactamente igual que antes (sin secciones vacías raras en el ticket).

---

## Notas para Claude Code

- No reinventar patrones: el CRUD de Expense debe seguir exactamente la misma estructura ya usada para Cashier o RestaurantTable en specs anteriores (entidad simple, repositorio sin queries complejas salvo el filtro por cashRegisterId, servicio con validaciones básicas, controlador REST estándar).
- Verificar que el bug de zona horaria ya corregido (toIsoDate en ReportsPage.jsx, fix aplicado para el preset "Hoy") no afecte este cálculo: los gastos se filtran por cashRegisterId, no por fecha, así que no debería haber riesgo de ese mismo tipo de bug aquí, pero vale la pena confirmarlo.
- Después de implementar, borrar backend/heladeria.db (tabla nueva expenses) y verificar que mvn spring-boot:run y npm run build corran limpio.