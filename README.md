# Clínica Veterinaria Huellas

Prototipo de consola para registrar clientes, mascotas, turnos y consultas. Retoma el proyecto presentado en el trabajo práctico 1 de Seminario de Práctica de Informática. La base de datos es MySQL y la aplicación usa Java con JDBC.

## Qué se puede probar

- Registrar un cliente y asociarle una mascota.
- Consultar los veterinarios disponibles.
- Agendar turnos de 30 minutos. La base impide que un mismo veterinario tenga dos turnos en el mismo horario.
- Listar la agenda de una fecha, con filtro opcional por veterinario.
- Registrar una consulta para un turno confirmado y consultar la historia clínica de la mascota, incluidas las alergias y las vacunas cargadas en MySQL.

El esquema también deja previstas las vacunas, las bajas lógicas, los usuarios y los recordatorios. La consola todavía no incluye ingreso con contraseña, cancelación y reprogramación de turnos, carga de vacunas ni envío de mensajes. Estas funciones están identificadas como trabajo pendiente en el informe.

## Preparación

Se necesita MySQL 8, un JDK 17 o posterior y MySQL Connector/J. Se puede trabajar en VS Code con las extensiones de Java.

1. Ejecutar `sql/01_esquema.sql` en MySQL.
2. Crear un usuario de MySQL con permisos sobre la base `huellas` y guardar su contraseña.
3. Cargar `sql/02_datos_y_consultas.sql` si se quieren datos de ejemplo. Ese archivo sirve para una base de prueba; al volver a ejecutarlo se repetirán los registros y el DNI de ejemplo dará error.
4. Definir las variables de entorno `HUELLAS_DB_USER` y `HUELLAS_DB_PASSWORD`. Si la base está en otro equipo, definir también `HUELLAS_DB_URL`.

Un administrador de MySQL puede crear el usuario de prueba con estas sentencias. Hay que reemplazar el texto de la contraseña antes de ejecutarlas:

```sql
CREATE USER 'huellas_app'@'localhost' IDENTIFIED BY 'CAMBIAR_ESTA_CLAVE';
GRANT SELECT, INSERT, UPDATE, DELETE ON huellas.* TO 'huellas_app'@'localhost';
```

Ejemplo para PowerShell, cambiando los valores por los de la instalación local:

```powershell
$env:HUELLAS_DB_USER = 'huellas_app'
$env:HUELLAS_DB_PASSWORD = 'clave_local'
$env:HUELLAS_DB_URL = 'jdbc:mysql://localhost:3306/huellas?serverTimezone=America/Argentina/Cordoba'
```

Para compilar y ejecutar desde una terminal de VS Code, reemplazar la ruta del conector por la del archivo descargado:

```powershell
javac -encoding UTF-8 -d out src\*.java
java -cp "out;C:\ruta\mysql-connector-j.jar" HuellasApp
```

## Estructura

- `src/HuellasApp.java`: menú de consola.
- `src/HuellasService.java`: validaciones de las operaciones.
- `src/HuellasRepository.java`: consultas JDBC y transacción de atención.
- `src/Conexion.java`: conexión a MySQL.
- `sql/01_esquema.sql`: tablas, claves, índices y restricciones.
- `sql/02_datos_y_consultas.sql`: carga de ejemplo, consultas y borrado de un registro de prueba.

## Pruebas manuales

| Caso | Acción | Resultado esperado |
|---|---|---|
| P01 | Registrar cliente y mascota | Se muestran sus números de registro. |
| P02 | Agendar un turno futuro | Se confirma el número de turno. |
| P03 | Repetir el mismo horario y veterinario | Se informa que el horario ya está ocupado. |
| P04 | Agendar el mismo horario con otro veterinario | Se permite la reserva. |
| P05 | Registrar una consulta del turno confirmado | La consulta aparece en la historia y el turno queda atendido. |
| P06 | Repetir la atención del mismo turno | Se informa que el turno ya fue cerrado. |
| P07 | Consultar una fecha sin turnos | Aparece un mensaje informativo. |
| P08 | Filtrar la agenda por un veterinario | Aparecen solo sus turnos. |

Estas pruebas requieren MySQL y el conector en funcionamiento. El informe deja asentado qué se verificó en este entorno y qué debe ejecutarse al preparar la instalación local.
