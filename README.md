# MotorCorp — Sistema de Gestión del Taller

Aplicación de escritorio en Java para el taller de motos **MotorCorp** (Huancayo). Gestiona clientes y sus motos, la cola de atención del taller, el historial de reparaciones, el inventario, el punto de venta y los reportes.

Proyecto del curso **Integrador I: Sistemas Software** — Universidad Tecnológica del Perú (UTP), 2026.

## Módulos

| Módulo | Qué hace |
|---|---|
| Inicio de sesión | Usuario y contraseña, bloqueo tras 3 intentos fallidos, registro de administrador con código secreto |
| Panel principal | Ventas del día, ingresos del mes, motos en el taller y productos con stock bajo |
| Clientes | Registro de clientes y sus motos, búsqueda, edición y eliminación |
| Cola de espera | Ingreso de motos por placa, orden de llegada, asignación de mecánico y estados |
| Historial del taller | Diagnóstico, repuestos usados y cierre del servicio con descuento de stock |
| Inventario | Productos (motos, accesorios y repuestos), stock bajo y reposición |
| Punto de venta | Cobro de órdenes finalizadas y emisión de comprobantes |
| Reportes | Ventas, atenciones e inventario por fechas, con exportación a Excel |

Cada rol ve solo sus módulos: **Administrador** (todos), **Recepción** (todos excepto Reportes) y **Mecánico** (Inicio, Cola, Inventario en modo consulta e Historial).

## Tecnologías

- Java 26 (JDK 26) y Apache Maven
- Apache NetBeans (GUI Builder) y Swing con tema FlatLaf
- PostgreSQL 18 con JDBC
- Librerías: Google Guava, Apache POI, Apache Commons (Lang y Codec) y Logback
- Pruebas: JUnit 5 (TDD)

## Arquitectura

- **MVC:** paquetes `modelo`, `vista` y `controlador`.
- **DAO:** una interfaz por entidad (`ClienteDAO`, `ProductoDAO`, ...) y su implementación JDBC con `PreparedStatement`.
- **SOLID:** responsabilidad única por clase, permisos por rol extensibles, interfaces pequeñas e inyección de dependencias por constructor.
- **Transacciones ACID:** registro de cliente con moto, cierre de servicio con stock y emisión de comprobantes (`commit` / `rollback`).
- **Seguridad:** contraseñas con SHA-256 y sal, bloqueo por intentos, permisos por rol y bitácora con Logback (`logs/motorcorp.log`).

```
src/main/java
├── controlador   controladores de cada módulo
├── dao           interfaces DAO, implementaciones y Conexion
├── modelo        entidades (Cliente, Vehiculo, Producto, OrdenServicio, ...)
├── motorcorp     clase principal (MotorCorp.java)
├── util          validaciones, seguridad, permisos, formato y Excel
└── vista         pantallas (FrmLogin, FrmPrincipal, Pnl...)
src/test/java/util  pruebas JUnit 5
database/motorcorp_db.sql  script de la base de datos
```

## Instalación

1. Instalar **JDK 26**, **Apache NetBeans** y **PostgreSQL 18**.
2. Clonar el repositorio:
   ```
   git clone https://github.com/Seiler27/MotorCorp.git
   ```
3. En pgAdmin, crear la base de datos `motorcorp_db` y ejecutar `database/motorcorp_db.sql`.
4. Copiar `src/main/resources/db.properties.example` como `db.properties` en la misma carpeta y completar los datos de su PostgreSQL:
   ```
   db.url=jdbc:postgresql://localhost:5432/motorcorp_db
   db.usuario=postgres
   db.password=SU_CONTRASEÑA
   ```
   Este archivo no se sube a GitHub (está en `.gitignore`).
5. Abrir el proyecto en NetBeans, ejecutar **Clean and Build** (Maven descarga las librerías) y luego **Run** (clase principal `motorcorp.MotorCorp`).

## Usuarios de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| admin | Admin123 | Administrador |
| recepcion | Recep123 | Recepción |
| mecanico1 | Meca123 | Mecánico |
| mecanico2 | Meca123 | Mecánico |

## Pruebas

52 pruebas automáticas con JUnit 5 en `src/test/java/util` (Validador, Seguridad, ControlIntentos, Formato, Permisos, ValidacionCliente, ValidacionProducto y ExportadorExcel). Para ejecutarlas: clic derecho en el proyecto → **Test**, o `mvn test`.

## Integrantes

- Reynoso Ruiz, Diego Eduardo

Docente: Maria Elena Tasa Catanzaro
