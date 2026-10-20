# Clínica Veterinaria Huellas

Prototipo de consola en Java con MySQL para el trabajo práctico 3 de Seminario de Práctica de Informática. Continúa el sistema de turnos e historia clínica presentado en los trabajos anteriores.

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
0. Salir.

La autenticación, la cancelación y reprogramación de turnos, las bajas desde el menú y el envío de recordatorios quedan pendientes. Las tablas previstas para esas funciones se conservan. No se incorporan facturación ni portal web.

## Preparación

Se necesita MySQL 8, un JDK 17 o posterior y MySQL Connector/J. Se puede trabajar en VS Code con las extensiones de Java.

1. En una instalación nueva, ejecutar `sql/01_esquema.sql`. Si ya se usó el TP2, se conserva la misma base: esta entrega no necesita migraciones.
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
$fuentes = @(Get-ChildItem .\src\*.java | ForEach-Object FullName)
javac -encoding UTF-8 --release 17 -d out $fuentes
java -cp 'out;C:\DevTools\mysql-connector-j-26.7.0\mysql-connector-j-26.7.0.jar' HuellasApp
```

En la ejecución manual hay que definir previamente `HUELLAS_DB_PASSWORD` en el entorno. El script evita tener que escribirla en un comando.

## Organización y POO

- `Persona` es abstracta. `Cliente` y `Veterinario` heredan su identidad y nombre, y redefinen `descripcion()`. El menú invoca ese método mediante una referencia de tipo `Persona` al mostrar profesionales y responsables de mascotas.
- `Mascota`, `Turno`, `Consulta` y `Vacuna` tienen atributos privados y constructores que validan los datos. `Turno.atender()` controla el cambio de estado; no hay un setter público que permita cerrarlo arbitrariamente.
- `HuellasService` coordina las operaciones; los seis archivos `*Repository.java` concentran el SQL de cada entidad. `Conexion` abre JDBC y `Jdbc` contiene ayudas de lectura y claves generadas.
- `HistoriaClinica` reúne la mascota y copias no modificables de las listas de consultas y vacunas.
- `Algoritmos` busca nombres con un recorrido lineal y ordena la agenda por inserción. Son operaciones pensadas para el volumen reducido de este prototipo; la búsqueda completa carga el catálogo en memoria.
- `HuellasApp` muestra los resultados y recupera errores de fechas, números, validación y SQL. `Fechas` exige fechas reales, sin ajustar automáticamente un día inexistente.

Las relaciones entre registros se representan con IDs y claves foráneas. Los IDs de clientes y veterinarios pertenecen a tablas distintas. El valor 0 identifica un objeto nuevo antes de guardarlo; las referencias a registros existentes deben ser positivas.

## Pruebas reproducibles

Sin MySQL:

```powershell
.\Ejecutar.ps1 -Pruebas
```

Con MySQL y Connector/J:

```powershell
.\Ejecutar.ps1 -Integracion -Conector 'C:\DevTools\mysql-connector-j-26.7.0\mysql-connector-j-26.7.0.jar'
```

`PruebasModelo` comprueba constructores, estados, polimorfismo, fechas y algoritmos. `PruebasConsola` ejecuta el programa como proceso y verifica entradas inválidas y fin de entrada. `PruebasIntegracion` crea registros con una marca exclusiva, consulta con nuevas conexiones y elimina únicamente sus propios IDs al terminar. Si se interrumpe a la fuerza, pueden quedar esos datos de prueba; no deben borrarse registros ajenos para limpiarlos.

La integración cubre DNI y turno duplicados, filtros, transacción de atención, vacunas, historia persistida y conservación de antecedentes tras la baja lógica. No reemplaza una prueba de carga ni una validación con personal de la clínica.

## Recorrido manual sugerido

Registrar un cliente y una mascota, buscarla en la opción 8, consultar un veterinario en la opción 3 y reservar un turno futuro. Verlo en la agenda, registrar la consulta y abrir la historia. Usar el número de consulta que aparece allí para cargar una vacuna. Al abrir nuevamente la historia, debe figurar la vacuna con sus fechas.

Las fechas simples usan `AAAA-MM-DD`; las reservas usan `DD/MM/AAAA HH:MM`. Los turnos comienzan en punto o y media. El peso usa punto decimal, es mayor que cero y admite hasta dos decimales. Para las demostraciones se permite registrar la atención de un turno futuro; no se simula el transcurso del tiempo ni se valida la presencia real del paciente.

## Verificación de esta entrega

El 20 de octubre de 2026 se ejecutaron 18 pruebas del modelo, 8 de consola y 27 de integración: 53 correctas. La compilación se hizo con JDK 27 y `--release 17`. La integración usó MySQL 8.0.46 y Connector/J 26.7.0 en una instancia temporal local del puerto 3307, con el mismo esquema del proyecto; no se modificó la base original. También se forzó una falla después del INSERT de consulta y se verificó desde otra conexión que no quedara una atención parcial. La instancia temporal se cerró después de las pruebas.
