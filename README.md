# Clínica Veterinaria Huellas

Prototipo de consola en Java con MySQL para el trabajo práctico 4 de Seminario de Práctica de Informática. Continúa el sistema de turnos e historia clínica presentado en los trabajos anteriores y reúne la versión integradora.

## Funciones del menú

1. Registrar cliente.
2. Registrar mascota de un cliente activo.
3. Consultar veterinarios activos.
4. Agendar un turno de 30 minutos.
5. Consultar la agenda por fecha, con filtro opcional por veterinario.
6. Registrar una consulta y pasar el turno de confirmado a atendido en una misma transacción.
7. Consultar la historia clínica, las alergias y las vacunas, incluso si la mascota tiene baja lógica.
8. Buscar mascotas por parte del nombre y ver su responsable.
9. Registrar una vacuna asociada a una consulta, con fecha opcional de refuerzo.
10. Buscar clientes por nombre o DNI.
11. Cancelar un turno confirmado, indicando el motivo.
12. Reprogramar un turno confirmado, conservando su ID.
13. Consultar el reporte de atenciones de un período.
14. Guardar un reporte en un archivo nuevo.
15. Abrir un reporte guardado.
0. Salir.

La autenticación, las bajas desde el menú, las alertas de vacunas y el envío de recordatorios quedan pendientes. Las tablas previstas para esas funciones se conservan. No se incorporan facturación ni portal web.

## Preparación

Se necesita MySQL 8, un JDK 17 o posterior y MySQL Connector/J. Se puede trabajar en VS Code con las extensiones de Java.

1. En una instalación nueva, ejecutar `sql/01_esquema.sql`. Si ya se usó TP2 o TP3, se conserva la misma base: esta entrega no necesita migraciones.
2. Crear un usuario de MySQL con permisos sobre la base `huellas`.
3. Ejecutar `sql/02_datos_y_consultas.sql` solamente si se necesitan los datos de ejemplo. No es un script para repetir: el DNI y la matrícula de ejemplo son únicos y su sección final modifica la mascota de muestra.
4. Definir `HUELLAS_DB_USER` si el usuario no es `huellas_app`. Definir `HUELLAS_DB_URL` si la base no está en `localhost:3306/huellas`.

Un administrador puede crear el usuario con estas sentencias, reemplazando el texto de la contraseña:

```sql
CREATE USER 'huellas_app'@'localhost' IDENTIFIED BY 'CAMBIAR_ESTA_CLAVE';
GRANT SELECT, INSERT, UPDATE, DELETE ON huellas.* TO 'huellas_app'@'localhost';
```

## Ejecutar desde VS Code

Abrir la carpeta del proyecto y una terminal PowerShell. El JDK debe estar en `PATH` o indicado en `JAVA_HOME`.

```powershell
.\Ejecutar.ps1 -Conector 'C:\DevTools\mysql-connector-j-26.7.0\mysql-connector-j-26.7.0.jar'
```

El script compila y pide la contraseña de MySQL de forma oculta si no está definida en el entorno. Ajustar la ruta si el conector está en otra carpeta. El programa conserva los datos entre ejecuciones.

Para compilar a mano, PowerShell necesita expandir los archivos, especialmente si la ruta contiene espacios:

```powershell
$fuentes = @(Get-ChildItem .\src -Recurse -Filter *.java | ForEach-Object FullName)
javac -encoding UTF-8 --release 17 -d out $fuentes
java -cp 'out;C:\DevTools\mysql-connector-j-26.7.0\mysql-connector-j-26.7.0.jar' HuellasApp
```

En la ejecución manual hay que definir previamente `HUELLAS_DB_PASSWORD` en el entorno. El script evita tener que escribirla en un comando.

## Organización y patrón MVC

- `src/HuellasApp.java` inicia el controlador.
- `huellas.vista.VistaConsola` muestra el menú, pide datos y presenta objetos, agenda, historia y reportes. El controlador no escribe directamente en `System.out`.
- `huellas.controlador.HuellasController` elige la operación, convierte las entradas y coordina servicio y vista. Recupera errores de datos, SQL y archivos.
- `huellas.modelo` contiene Persona, Cliente, Veterinario, Mascota, Turno, Consulta, Vacuna, HistoriaClinica y los tipos de reporte. Persona es abstracta; Cliente y Veterinario redefinen descripcion(), invocado mediante Persona.
- `huellas.servicio` conserva las reglas y los algoritmos de búsqueda e inserción. HuellasService coordina los repositorios.
- `huellas.persistencia` es el paquete JDBC: Conexion, Jdbc y seis repositorios por entidad. Usa sentencias preparadas y transacciones.
- `huellas.archivos.ArchivoReporte` guarda y recupera reportes sin alterar la base.

El patrón MVC separa modelo, vista y controlador. La persistencia por entidad complementa esa estructura. Los modelos mantienen atributos privados y constructores que validan datos. Turno controla sus cambios de estado y horario. Los IDs pertenecen a cada tabla; el ID 0 indica un objeto nuevo antes de guardar.

## Turnos y búsquedas

La opción 10 permite recuperar el ID del cliente a partir de su nombre o DNI. Ese ID es el que pide el alta de mascota; no hay que ingresar el teléfono como número de cliente.

Solo un turno CONFIRMADO puede cancelarse o reprogramarse. La cancelación guarda su motivo y libera el horario. La reprogramación conserva el ID y exige una fecha futura en punto o y media. Si el nuevo horario está ocupado, la transacción se revierte y el turno conserva su fecha anterior. Los turnos ATENDIDOS, CANCELADOS o AUSENTES no admiten estos cambios.

## Reportes, arreglos y ArrayList

El reporte usa la fecha real de atención de `consultas.atendida_en` y un período con ambos extremos incluidos. `ReporteConsultas` guarda las filas en un ArrayList y calcula sus cantidades en un arreglo `long[7]`, con índices de lunes a domingo. La vista recorre esos valores junto con un arreglo de nombres de días. Los resultados y el arreglo se devuelven mediante copias para evitar cambios externos.

La opción 14 pide una ruta para un archivo nuevo, por ejemplo `reporte-noviembre.tsv`. La carpeta debe existir. La opción 15 recupera ese archivo y reconstruye el resumen, sin insertar ni actualizar registros en MySQL. Los reportes se escriben con UTF-8 y no sobrescriben archivos existentes.

El formato TSV tiene una primera línea `HUELLAS_CONSULTAS_V1` con las fechas del período y una segunda línea con los nombres de seis columnas. Cada fila contiene ID de consulta, ID de mascota, nombre de mascota y veterinario en Base64, fecha de atención y peso. Base64 conserva tabulaciones y saltos de línea dentro de los nombres; no cifra los datos. Se valida la versión, la cabecera, el número de campos, los IDs, las fechas, el período, el peso y los IDs duplicados. El archivo no incluye DNI, teléfono, diagnóstico ni tratamiento.

## Pruebas reproducibles

Sin MySQL:

```powershell
.\Ejecutar.ps1 -Pruebas
```

Con MySQL y Connector/J:

```powershell
.\Ejecutar.ps1 -Integracion -Conector 'C:\DevTools\mysql-connector-j-26.7.0\mysql-connector-j-26.7.0.jar'
```

`PruebasModelo` comprueba constructores, estados, polimorfismo, fechas y algoritmos. `PruebasConsola` ejecuta el programa como proceso y verifica entradas inválidas y fin de entrada. `PruebasTP4` verifica los nuevos estados, arreglos, reportes y errores de archivos. `PruebasIntegracion` crea registros con una marca exclusiva, consulta con nuevas conexiones y elimina únicamente sus propios IDs al terminar. Si se interrumpe a la fuerza, pueden quedar esos datos de prueba; no deben borrarse registros ajenos para limpiarlos.

La integración cubre DNI y turno duplicados, filtros, transacción de atención, vacunas, historia persistida, conservación de antecedentes, búsqueda de clientes, cancelación, reprogramación, rollback del horario y el menú real con reportes. No reemplaza una prueba de carga ni una validación con personal de la clínica.

## Recorrido manual sugerido

Registrar un cliente y una mascota, buscarla en la opción 8, consultar un veterinario en la opción 3 y reservar un turno futuro. Verlo en la agenda, registrar la consulta y abrir la historia. Usar el número de consulta que aparece allí para cargar una vacuna. Al abrir nuevamente la historia, debe figurar la vacuna con sus fechas.

Las fechas simples usan `AAAA-MM-DD`; las reservas usan `DD/MM/AAAA HH:MM`. Los turnos comienzan en punto o y media. El peso usa punto decimal, es mayor que cero y admite hasta dos decimales. Para las demostraciones se permite registrar la atención de un turno futuro; no se simula el transcurso del tiempo ni se valida la presencia real del paciente.

## Verificación de esta entrega

El 6 de noviembre de 2026 se ejecutaron 18 pruebas del modelo, 12 de consola, 20 de las funciones nuevas de TP4 y 38 de integración: 88 correctas. Se compiló con JDK 27 y `--release 17`. La integración usó MySQL 8.0.46 y Connector/J 26.7.0 en una instancia temporal local del puerto 3308, con el mismo esquema y un usuario con permisos SELECT, INSERT, UPDATE y DELETE. No se modificó la base original. Al terminar se verificó que no quedaran registros de prueba y se cerró la instancia.

Las pruebas de archivos incluyen lectura posterior al guardado, texto con separadores y acentos, reportes vacíos, archivos incompletos, inexistentes o con texto UTF-8 dañado y rechazo de sobrescritura. Las de transacciones incluyen fallas posteriores al INSERT de una consulta y reprogramaciones a un horario ocupado.
