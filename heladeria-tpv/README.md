# Heladeria TPV

Sistema de punto de venta de escritorio para la heladeria. Corre 100% local,
sin internet ni servidores externos. Backend en Spring Boot, frontend en
React, empaquetado como app de escritorio con Electron, y base de datos
SQLite (un solo archivo).

## Estructura

```
heladeria-tpv/
├── backend/    Spring Boot + SQLite (API REST)
├── frontend/   React + Vite (interfaz del TPV)
└── electron/   Cascaron de escritorio (junta todo en un .exe)
```

## Requisitos para desarrollar

- Java 17 o superior
- Maven
- Node.js 18 o superior

## 1. Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

Queda corriendo en `http://localhost:8080`. El primer arranque crea
automaticamente un usuario administrador con PIN `0000` (cambialo desde la
app apenas entres).

La base de datos SQLite se crea en la carpeta actual como `heladeria.db`
cuando corres asi, en modo desarrollo. En la app empaquetada, se crea en una
carpeta de datos del usuario para que sobreviva actualizaciones.

Para probar los endpoints con Postman/EchoAPI, la URL base es
`http://localhost:8080/api`.

## 2. Frontend (React)

En otra terminal:

```bash
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173` en el navegador. Necesita el backend corriendo
al mismo tiempo (paso anterior).

## 3. Generar el instalador (.exe)

Primero compila el backend a un .jar:

```bash
cd backend
mvn clean package -DskipTests
```

Esto genera `backend/target/heladeria-tpv.jar`.

Luego compila el frontend:

```bash
cd frontend
npm run build
```

Esto genera `frontend/dist/`.

Finalmente, empaqueta todo con Electron:

```bash
cd electron
npm install
npm run dist
```

El instalador queda en `electron/dist/`. Ese es el archivo que se lleva a la
heladeria e instala una sola vez.

### Sobre el JRE empaquetado

Para que el instalador no dependa de que el PC de la heladeria tenga Java
instalado, lo ideal es incluir un JRE portable dentro de `electron/jre/`.
Si esa carpeta esta vacia, la app intentara usar el Java del sistema — lo
cual funciona si Java ya esta instalado, pero no es lo mas robusto para
distribuir a otra persona. Para generar un JRE portable minimo:

```bash
# Desde el JDK que tengas instalado (17+), genera un runtime reducido
jlink --add-modules java.base,java.desktop,java.logging,java.naming,java.management,java.sql,java.security.jgss,jdk.crypto.ec --output electron/jre --strip-debug --no-header-files --no-man-pages
```

Si por ahora no quieres hacer esto, no es necesario: mientras pruebes en tu
propia maquina (que ya tiene Java por Cursor/desarrollo), la app funciona
igual usando el Java instalado.

## Usuarios y roles

- **ADMIN**: gestiona productos, ve reportes, abre/cierra turno.
- **CAJERO**: vende, cobra, anula ventas, abre/cierra turno.

El login es por PIN numerico, sin usuario ni contraseña de texto.

## Modulos incluidos (Fase 1)

- Ventas para llevar (sin mesas), con catalogo de productos
- Cobro en efectivo (con calculo de cambio y botones de billete),
  Nequi y Rappi
- Anulacion de ventas confirmadas, con trazabilidad de quien anulo
- Turnos de caja (apertura/cierre por fecha y hora, sin montos de dinero)
- Reportes de ventas por metodo de pago, con filtro de rango de fechas
  (hoy, semana, mes, personalizado)
- Barra en vivo de ventas del turno actual, desglosada por metodo de pago

## Pendiente para una Fase 2 (no incluido ahora)

- Inventario de insumos con descuento automatico por receta
- IVA / impuestos desglosados
- Combos y descuentos
- Backup/restauracion de la base de datos desde la interfaz
