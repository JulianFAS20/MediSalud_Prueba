# MediSalud API

Backend REST para registrar médicos y pacientes, consultar disponibilidad, reservar, cancelar, reprogramar y filtrar citas médicas.

El proyecto prioriza reglas de negocio explícitas, aislamiento del dominio, consistencia transaccional y protección ante reservas concurrentes.

## Inicio rápido

### Requisitos

- Java 21
- Maven 3.9+
- Docker para ejecutar las pruebas PostgreSQL con Testcontainers; Docker Compose solo para levantar el entorno PostgreSQL manual

Docker no es obligatorio para iniciar la API con H2. Si no está disponible, JUnit omite explícitamente
la suite Testcontainers y conserva las pruebas unitarias e integración H2.

El proyecto usa Spring Boot 3.5.15 y sobrescribe únicamente Tomcat a 10.1.57 para
incorporar correcciones de seguridad publicadas después del BOM de esa versión.

### Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje y framework | Java 21, Spring Boot 3.5.15, Spring Web MVC |
| Validación | Jakarta Bean Validation y validaciones de dominio |
| Persistencia | Spring Data JPA, Hibernate, Flyway |
| Bases de datos | H2 para ejecución local y PostgreSQL 17 para un entorno productivo simulado |
| Calidad | JUnit 5, Mockito, AssertJ, MockMvc, Testcontainers, ArchUnit y JaCoCo |
| Productividad | Lombok, Maven y Postman |
| Operación | Docker, Docker Compose y GitHub Actions |

### Perfiles disponibles

| Perfil | Uso | Reloj | Base de datos |
|---|---|---|---|
| `local` | Desarrollo y evaluación rápida; es el perfil predeterminado | Hora real | H2 en memoria |
| `postgres` | Ejecución contra PostgreSQL | Hora real | PostgreSQL |
| `local,rn05` | Prueba manual determinista de penalizaciones | Fijo: 2030-01-16 10:20, Bogotá | H2 en memoria |

### Ejecutar localmente con H2

El perfil local es el perfil predeterminado. No requiere instalar una base de datos.

Antes de iniciar, verifique que el puerto `8080` esté libre y que ningún otro proceso lo esté utilizando.
Si el puerto está ocupado, detenga el proceso correspondiente o configure otro mediante `SERVER_PORT`;
en ese caso también debe actualizar la variable `baseUrl` de Postman.

```bash
mvn spring-boot:run
```

La API queda disponible en http://localhost:8080. La consola de H2 está en http://localhost:8080/h2-console con:

- JDBC URL: jdbc:h2:mem:medisalud
- Usuario: sa
- Contraseña: vacía

H2 es volátil en este perfil: al detener la aplicación se eliminan médicos adicionales, pacientes, citas y
penalizaciones. Los tres médicos semilla se vuelven a cargar en el siguiente arranque.

También puede validar, empaquetar y ejecutar el artefacto:

```bash
mvn clean verify
java -jar target/medisalud-api-1.0.0.jar
```

### Ejecutar con PostgreSQL

El perfil `postgres` no contiene credenciales predeterminadas: exige `DB_URL`, `DB_USERNAME` y
`DB_PASSWORD`. Para Compose, copie el ejemplo ignorado por Git y complete un usuario y una contraseña
exclusivos de su entorno:

```bash
cp .env.example .env
```

En PowerShell use `Copy-Item .env.example .env`. El puerto `5432` debe estar libre si se publica
PostgreSQL en el host. Después de completar `.env`, puede iniciar solamente la base:

```bash
docker compose up -d postgres
```

En PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="postgres"
$env:DB_URL="jdbc:postgresql://localhost:5432/medisalud"
$env:DB_USERNAME="<usuario>"
$env:DB_PASSWORD="<contraseña>"
mvn spring-boot:run
```

En Bash:

```bash
SPRING_PROFILES_ACTIVE=postgres \
DB_URL="jdbc:postgresql://localhost:5432/medisalud" \
DB_USERNAME="<usuario>" \
DB_PASSWORD="<contraseña>" \
mvn spring-boot:run
```

Para construir y ejecutar API y PostgreSQL como contenedores:

```bash
mvn clean package
docker compose --profile app up --build
```

`.env` está excluido del repositorio. Compose construye internamente `DB_URL` y se detiene antes de crear
contenedores cuando `DB_USERNAME` o `DB_PASSWORD` faltan o están vacíos.

## Arquitectura

Se utiliza Arquitectura Hexagonal con DDD ligero:

    com.medisalud
    ├── domain
    │   ├── model          Entidades, value objects y enums
    │   ├── service        Strategy de penalización y política horaria
    │   ├── port           Puertos de repositorio y calendario
    │   └── exception      Excepciones libres de Spring
    ├── application
    │   ├── command/dto    Contratos de entrada y salida
    │   ├── validation     Cadena de validadores de reserva
    │   ├── port           Unidad de trabajo transaccional
    │   └── usecase        Casos de uso
    └── infrastructure
        ├── adapter/in      Controllers REST y manejo de errores
        ├── adapter/out     JPA, calendario y transacciones
        └── config          Composición de dependencias y datos iniciales

La dirección principal de dependencias es:

    REST/JPA/configuración -> aplicación -> dominio <- puertos/adaptadores

El dominio no conoce Spring, Jakarta, HTTP ni JPA. Los casos de uso tampoco conocen infraestructura. Estas restricciones se validan automáticamente con ArchUnit.

Esta arquitectura encaja bien porque las reglas de horarios, conflictos, penalizaciones y reprogramación cambian con mayor frecuencia que el mecanismo HTTP o la base de datos. Es posible reemplazar H2/PostgreSQL, el calendario o la API sin reescribir el núcleo.

### Patrones y decisiones relevantes

- Dominio rico: Cita controla la transición PROGRAMADA → CANCELADA; Paciente valida la fecha de nacimiento al reservar; FranjaHoraria garantiza una duración de 30 minutos.
- Política horaria única: `PoliticaHorarioAtencion` define días laborables y jornadas; `JornadaAtencion` genera y valida franjas usando `FranjaHoraria.DURACION`. Disponibilidad y reserva consumen la misma regla.
- Chain of Responsibility: las reglas de reserva son validadores SRP independientes. Agregar una nueva regla no modifica ReservarCitaUseCase.
- Strategy: PenalizacionCancelacionStrategy aísla RN-05 del flujo de cancelación.
- Unit of Work: cancelación + penalización + nueva reserva se ejecutan en una transacción. Una reprogramación fallida revierte todo.
- Concurrencia: además de consultar disponibilidad, la base usa claves únicas anulables para la franja activa de médico y paciente. Dos solicitudes simultáneas no pueden confirmar la misma franja.
- Paginación: el dominio define `Paginacion` y `Pagina<T>` sin depender de Spring; el adaptador JPA traduce el contrato a `PageRequest` con orden estable por fecha e ID.
- Persistencia: Flyway administra el esquema; Hibernate se limita a validarlo. Las mismas migraciones y adaptadores se verifican contra H2 y PostgreSQL real.
- Identificadores: UUID evita acoplamiento a secuencias y facilita distribución futura.
- Tiempo: la API exige ISO 8601 con offset, el dominio persiste Instant y las reglas laborales se evalúan en America/Bogota.

### Decisiones sobre ambigüedades

- RN-04 contiene una contradicción entre “mismo médico” y “aunque sea otro médico”. Se aplica la alternativa segura: un paciente no puede tener dos citas programadas en la misma franja, incluso con médicos distintos.
- Fecha de nacimiento es opcional porque RF-02 no la exige. Si falta, RN-03 asume edad cero; si está en el futuro, la reserva se rechaza.
- La consulta de disponibilidad usa un rango inclusivo de hasta 90 días calendario y no devuelve franjas pasadas.
- fechaInicio y fechaFin del listado se comparan como instantes; ambos filtros son inclusivos.
- `ATENDIDA` se conserva como estado consultable porque RF-06 lo define, pero el MVP no expone una transición a atendida porque ningún requerimiento solicita ese endpoint.

## Cobertura de requerimientos

| Requerimiento | Implementación |
|---|---|
| RF-01 | Registro de médicos con campos obligatorios, opcionales y validación de formato. |
| RF-02 | Registro de pacientes y unicidad del documento en aplicación/persistencia. |
| RF-03 | Reserva con referencias válidas, fecha ISO 8601 con offset y estado inicial `PROGRAMADA`. |
| RF-04 | Disponibilidad inclusiva por médico y rango acotado, en intervalos libres de 30 minutos. |
| RF-05 | Cancelación, registro de `canceladaEn` y evaluación transaccional de penalización. |
| RF-06 | Listado paginado con filtros combinables por médico, paciente, estado y rango de instantes. |
| RN-01 | Validador de horario laboral, domingo y calendario de festivos colombianos. |
| RN-02 | Consulta preventiva y restricción única de franja activa por médico. |
| RN-03 | Validación de fecha de nacimiento al reservar; ausencia equivale a edad cero. |
| RN-04 | Restricción de una cita programada por paciente y franja, incluso con otro médico. |
| RN-05 | Strategy de cancelación tardía y bloqueo con tres penalizaciones en ventana móvil de 30 días. |
| RN-06 | Cancelación y nueva reserva dentro de una única transacción con rollback ante fallos. |

## Horarios y festivos

La clínica opera:

- Lunes a viernes: 08:00–18:00
- Sábado: 08:00–13:00
- Domingo y festivos: sin atención

El adaptador calcula los festivos nacionales colombianos para cualquier año: fechas fijas, traslados al lunes, Jueves/Viernes Santo y festividades relativas a Pascua. Incluye desde 2026 el festivo de Nuestra Señora del Rosario de Chiquinquirá. La base legal está en la [Ley 51 de 1983](https://www.suin-juriscol.gov.co/viewDocument.asp?ruta=Leyes%2F1605519) y la [Ley 2578 de 2026](https://www.suin-juriscol.gov.co/viewDocument.asp?id=30056513).

Se pueden sumar cierres extraordinarios en `application.yml`:

```yaml
medisalud:
  festivos:
    - 2026-09-10
  maximo-dias-disponibilidad: 90
  maximo-tamanio-pagina-citas: 100
```

Los límites también pueden sobrescribirse con `MEDISALUD_MAXIMO_DIAS_DISPONIBILIDAD` y
`MEDISALUD_MAXIMO_TAMANIO_PAGINA_CITAS`. Ambos deben ser mayores que cero; una configuración inválida
impide iniciar la aplicación.

## Datos iniciales

| ID | Médico | Especialidad |
|---|---|---|
| 00000000-0000-0000-0000-000000000001 | Dra. Maria Gonzalez | Cardiologia |
| 00000000-0000-0000-0000-000000000002 | Dr. Carlos Ruiz | Pediatria |
| 00000000-0000-0000-0000-000000000003 | Dra. Ana Lopez | Dermatologia |

Los datos se insertan solo cuando la tabla de médicos está vacía.

## Colección Postman

La raíz del proyecto incluye una colección de Postman con 63 escenarios agrupados en ocho carpetas:

- [`MediSalud.postman_collection.json`](./MediSalud.postman_collection.json): colección principal.
- [`MediSalud.RN05.postman_environment.json`](./MediSalud.RN05.postman_environment.json): ambiente auxiliar para ejecutar RN-05 con reloj fijo.

La colección contiene los siete servicios públicos, captura automáticamente los UUID creados y
genera documentos y fechas futuras para probar los flujos sin editar cada solicitud.

Para ejecutarla:

1. Iniciar la API en `http://localhost:8080`.
2. Importar `MediSalud.postman_collection.json` en Postman.
3. Ejecutar primero la carpeta `00 - Preparación` y continuar las carpetas en orden, o usar el Collection Runner.
4. Para repetir toda la ejecución desde cero con el perfil local, reiniciar la API para limpiar la base H2 en memoria.

Si el archivo de la colección cambia, debe importarse nuevamente; Postman no sincroniza automáticamente
los cambios realizados en el JSON local.

### Ejecutar RN-05 a cualquier hora

La penalización se registra cuando, al cancelar, faltan **menos de 2 horas** para iniciar la cita. La cita
también debe ser futura, comenzar en `:00` o `:30` y cumplir el horario laboral. Exactamente 2 horas no
genera penalización.

Fuera del horario laboral no es posible crear con el reloj real una cita que sea simultaneamente futura,
laboral y a menos de 2 horas. Para probar este caso de forma determinista, iniciar la API con H2 y el perfil
`rn05`:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local,rn05"
```

El perfil fija el reloj de la API en `2030-01-16T10:20:00-05:00`, exclusivamente para pruebas manuales.
En Postman, importar y seleccionar `MediSalud.RN05.postman_environment.json`. Luego ejecutar
`00 - Preparación` y solamente la carpeta `07 - Penalizaciones RN-05 (condicional)`.

Antes de enviar `1. Reservar cita tardia`, la esquina superior derecha de Postman debe mostrar
`MediSalud - RN05 reloj fijo`. Si muestra `No environment`, el pre-request script detiene la solicitud y
el backend no recibe ninguna llamada.

La solicitud `1. Reservar cita tardía` enviará una cita para las 11:30:

```json
{
  "pacienteId": "{{pacientePenalizacionId}}",
  "medicoId": "{{medicoSemilla3}}",
  "fechaHora": "2030-01-16T11:30:00-05:00"
}
```

Al cancelar, para la API siguen siendo las 10:20: faltan 70 minutos y se registra la penalización. La carpeta
repite reserva y cancelación tres veces; la última solicitud valida `409 PACIENTE_BLOQUEADO`. Al iniciar sin
el perfil `rn05`, el sistema vuelve automáticamente a `Clock.systemUTC()` y utiliza la hora real.

## API REST

La URL base local es `http://localhost:8080/api/v1`. Las fechas de disponibilidad usan `YYYY-MM-DD` y
los instantes de citas/filtros deben incluir offset ISO 8601, por ejemplo `2027-02-01T08:00:00-05:00`.
Las respuestas se expresan en la zona `America/Bogota`.

Los ejemplos de esta sección suponen el perfil `local` con reloj real. No deben ejecutarse con el perfil
`rn05`, cuyo reloj fijo está en 2030.

| Método | Endpoint | Entrada | Éxito |
|---|---|---|---|
| `POST` | `/medicos` | JSON de médico | `201 Created` |
| `POST` | `/pacientes` | JSON de paciente | `201 Created` |
| `POST` | `/citas` | paciente, médico y fecha/hora | `201 Created` |
| `GET` | `/medicos/{medicoId}/disponibilidad` | `fechaInicio` y `fechaFin`, máximo 90 días inclusivos | `200 OK` |
| `PATCH` | `/citas/{citaId}/cancelacion` | Sin body | `200 OK` |
| `POST` | `/citas/{citaId}/reprogramacion` | nueva fecha/hora | `201 Created` |
| `GET` | `/citas` | Filtros opcionales, `page` y `size` | `200 OK` |

Los UUID de esta sección son ilustrativos y cada ejemplo es independiente. Los comandos usan sintaxis
Bash/Git Bash; en PowerShell puede usar la colección Postman o invocar explícitamente `curl.exe`.

### Registrar médico

```bash
curl --request POST "http://localhost:8080/api/v1/medicos" \
  --header "Content-Type: application/json" \
  --data '{"nombreCompleto":"Dr. Juan Perez","especialidad":"Neurologia","telefono":"555-2000","email":"juan.perez@medisalud.com"}'
```

Respuesta `201 Created`:

```json
{
  "id": "81dd67e2-2149-4f74-9121-a739de39be91",
  "nombreCompleto": "Dr. Juan Perez",
  "especialidad": "Neurologia",
  "telefono": "555-2000",
  "email": "juan.perez@medisalud.com"
}
```

`telefono` y `email` son opcionales para médicos.

### Registrar paciente

```bash
curl --request POST "http://localhost:8080/api/v1/pacientes" \
  --header "Content-Type: application/json" \
  --data '{"nombreCompleto":"Laura Torres","documentoIdentidad":"1020304050","telefono":"3001234567","email":"laura@example.com","fechaNacimiento":"1990-01-15"}'
```

Respuesta `201 Created`:

```json
{
  "id": "d05bc052-9b4b-4023-bd1a-669f55aaebd8",
  "nombreCompleto": "Laura Torres",
  "documentoIdentidad": "1020304050",
  "telefono": "3001234567",
  "email": "laura@example.com",
  "fechaNacimiento": "1990-01-15"
}
```

`fechaNacimiento` es opcional. Un documento repetido devuelve `409 DOCUMENTO_DUPLICADO`.

### Reservar cita

```bash
curl --request POST "http://localhost:8080/api/v1/citas" \
  --header "Content-Type: application/json" \
  --data '{"pacienteId":"d05bc052-9b4b-4023-bd1a-669f55aaebd8","medicoId":"00000000-0000-0000-0000-000000000001","fechaHora":"2027-02-01T08:00:00-05:00"}'
```

Respuesta `201 Created`:

```json
{
  "id": "57edcb1c-c001-4623-98fb-7f7b17474382",
  "pacienteId": "d05bc052-9b4b-4023-bd1a-669f55aaebd8",
  "medicoId": "00000000-0000-0000-0000-000000000001",
  "fechaHora": "2027-02-01T08:00:00-05:00",
  "fechaHoraFin": "2027-02-01T08:30:00-05:00",
  "estado": "PROGRAMADA"
}
```

### Consultar disponibilidad

```bash
curl "http://localhost:8080/api/v1/medicos/00000000-0000-0000-0000-000000000001/disponibilidad?fechaInicio=2027-02-01&fechaFin=2027-02-06"
```

Respuesta `200 OK` abreviada:

```json
{
  "cantidadFranjasDisponibles": 1,
  "franjasDisponibles": [
    {
      "inicio": "2027-02-01T08:00:00-05:00",
      "fin": "2027-02-01T08:30:00-05:00"
    }
  ]
}
```

`cantidadFranjasDisponibles` siempre coincide con el tamaño de `franjasDisponibles` y representa
el total del rango solicitado. Cuando no existe disponibilidad, la API conserva `200 OK` y devuelve
`{"cantidadFranjasDisponibles": 0, "franjasDisponibles": []}`. El rango de fechas es inclusivo y
la respuesta real contiene todas las franjas libres futuras. Se admiten como máximo 90 días calendario;
un rango mayor devuelve `400 RANGO_DEMASIADO_AMPLIO` antes de consultar persistencia.

### Cancelar cita

```bash
CITA_ID="57edcb1c-c001-4623-98fb-7f7b17474382"
curl --request PATCH "http://localhost:8080/api/v1/citas/${CITA_ID}/cancelacion"
```

Respuesta `200 OK`:

```json
{
  "id": "57edcb1c-c001-4623-98fb-7f7b17474382",
  "pacienteId": "d05bc052-9b4b-4023-bd1a-669f55aaebd8",
  "medicoId": "00000000-0000-0000-0000-000000000001",
  "fechaHora": "2027-02-01T08:00:00-05:00",
  "fechaHoraFin": "2027-02-01T08:30:00-05:00",
  "estado": "CANCELADA",
  "canceladaEn": "2026-07-16T19:30:00-05:00"
}
```

Si faltan menos de dos horas para la cita, se registra exactamente una penalización.

### Reprogramar cita

El ID debe corresponder a una cita que aún esté `PROGRAMADA`; no reutilice una cita ya cancelada.

```bash
CITA_PROGRAMADA_ID="2f713a12-3cd1-4b42-a18d-071d4d7f4eaf"
curl --request POST "http://localhost:8080/api/v1/citas/${CITA_PROGRAMADA_ID}/reprogramacion" \
  --header "Content-Type: application/json" \
  --data '{"nuevaFechaHora":"2027-02-01T09:00:00-05:00"}'
```

Respuesta `201 Created`:

```json
{
  "id": "a22c3673-6a8a-4af6-bfb0-351cd1c613b0",
  "pacienteId": "d05bc052-9b4b-4023-bd1a-669f55aaebd8",
  "medicoId": "00000000-0000-0000-0000-000000000001",
  "fechaHora": "2027-02-01T09:00:00-05:00",
  "fechaHoraFin": "2027-02-01T09:30:00-05:00",
  "estado": "PROGRAMADA"
}
```

La cita anterior queda `CANCELADA`. Si cualquier validación falla, ambos cambios se revierten.

### Listar y filtrar citas

Todos los parámetros son opcionales y combinables:

```bash
curl "http://localhost:8080/api/v1/citas?medicoId=00000000-0000-0000-0000-000000000001&estado=PROGRAMADA&fechaInicio=2027-02-01T00:00:00-05:00&fechaFin=2027-02-28T23:59:59-05:00&page=0&size=20"
```

| Parámetro | Tipo | Valores |
|---|---|---|
| `medicoId` | UUID | Cualquier médico registrado |
| `pacienteId` | UUID | Cualquier paciente registrado |
| `estado` | enum | `PROGRAMADA`, `CANCELADA`, `ATENDIDA` |
| `fechaInicio` | ISO 8601 con offset | Límite inferior inclusivo |
| `fechaFin` | ISO 8601 con offset | Límite superior inclusivo |
| `page` | entero | Página base cero; valor predeterminado `0` |
| `size` | entero | Elementos por página; predeterminado `20` o el máximo configurado si es menor; máximo inicial `100` |

Respuesta `200 OK` abreviada:

```json
{
  "contenido": [
    {
      "id": "57edcb1c-c001-4623-98fb-7f7b17474382",
      "pacienteId": "d05bc052-9b4b-4023-bd1a-669f55aaebd8",
      "medicoId": "00000000-0000-0000-0000-000000000001",
      "fechaHora": "2027-02-01T08:00:00-05:00",
      "fechaHoraFin": "2027-02-01T08:30:00-05:00",
      "estado": "PROGRAMADA"
    }
  ],
  "pagina": 0,
  "tamanio": 20,
  "totalElementos": 1,
  "totalPaginas": 1,
  "primera": true,
  "ultima": true
}
```

La ordenación es ascendente por `fechaHora` y luego por `id`, lo que evita saltos o duplicados entre
páginas cuando varias citas tienen el mismo instante. `page < 0`, `size < 1` o un tamaño superior al
máximo devuelven `400` con un código de error explícito.

## Errores

Las excepciones de dominio se traducen en un contrato uniforme:

```json
{
  "timestamp": "2026-07-15T14:30:00Z",
  "status": 409,
  "error": "Conflict",
  "codigo": "FRANJA_MEDICO_OCUPADA",
  "mensaje": "El medico ya tiene una cita programada en esa franja",
  "path": "/api/v1/citas"
}
```

Cuando el error pertenece a campos de entrada, se agrega `erroresCampo` con elementos
`{"campo":"...","mensaje":"..."}`. Los campos nulos se omiten del JSON.

Mapeo HTTP:

- 400: formato, validación o regla de entrada inválida
- 404: médico, paciente, cita o ruta inexistente
- 405: método HTTP no permitido para la ruta
- 409: duplicidad, transición inválida, bloqueo por penalizaciones o carrera concurrente
- 415: Content-Type no soportado
- 500: fallo inesperado con mensaje seguro; el detalle queda solamente en logs

Los errores MVC estándar también conservan este contrato. Entre sus códigos están
`REQUEST_INVALIDO`, `JSON_INVALIDO`, `PARAMETRO_REQUERIDO`, `PARAMETRO_INVALIDO`,
`RUTA_NO_ENCONTRADA`, `METODO_NO_PERMITIDO` y `TIPO_CONTENIDO_NO_SOPORTADO`.

## Penalizaciones

Al cancelar una cita PROGRAMADA:

1. Se calcula la antelación con PenalizacionCancelacionStrategy.
2. Con menos de dos horas se registra una penalización ligada de forma única a la cita.
3. Al reservar se cuentan penalizaciones desde ahora menos 30 días.
4. Con tres o más, el paciente recibe 409 PACIENTE_BLOQUEADO.
5. El bloqueo desaparece naturalmente cuando suficientes penalizaciones salen de la ventana móvil.

## Pruebas

La suite contiene 115 pruebas automatizadas cuando Docker está disponible. Para ejecutarlas:

```bash
mvn test
```

La verificación completa genera el JAR, el reporte JaCoCo y comprueba los umbrales mínimos de
95 % de líneas y 85 % de ramas:

```bash
mvn clean verify
```

El reporte navegable queda en `target/site/jacoco/index.html`. La medición actual es 98,75 % de
líneas y 92,22 % de ramas.

### PostgreSQL real con Testcontainers

`PostgreSqlPersistenceIntegrationTest` inicia `postgres:17-alpine` en un puerto aleatorio, aplica Flyway
y destruye automáticamente el contenedor al finalizar. Para ejecutarla de forma aislada, Docker Engine
debe estar iniciado:

```bash
mvn -Dtest=PostgreSqlPersistenceIntegrationTest test
```

La prueba valida el tipo físico `TIMESTAMP WITH TIME ZONE`, conservación de `Instant`, restricciones
únicas, filtros combinados y paginados con Specifications, rollback de reprogramación y una carrera real
de dos transacciones sincronizadas. Ambas pasan la validación preventiva antes de insertar; PostgreSQL
confirma exactamente una y rechaza la otra con SQLState `23505`.

La suite incluye:

- Invariantes de `Cita`, normalización y validaciones de `Paciente` y `Medico`.
- Límites exactos de RN-01: apertura, última franja, sábado, domingo, festivo y alineación a 30 minutos.
- RN-02 y RN-04 en aplicación, REST y restricciones únicas de persistencia para solicitudes concurrentes.
- RN-03 con fecha de nacimiento ausente, actual y futura.
- RN-05 en los límites de dos horas y treinta días, además del ciclo integral de tres penalizaciones y bloqueo.
- Reprogramación exitosa y rollback completo cuando la nueva franja está ocupada.
- Flyway, tipos temporales, Specifications y rollback contra PostgreSQL 17 mediante Testcontainers.
- Concurrencia real con dos hilos sincronizados y una única reserva confirmada por la base de datos.
- Disponibilidad acotada a 90 días y paginación con límites, metadatos y filtros combinados.
- Contrato global de errores para dominio, Bean Validation, JSON, parámetros, persistencia, 404, 405, 415 y 500 seguros.
- Calendario colombiano, incluyendo Ley Emiliani, Pascua y el festivo creado en 2026.
- Restricciones de arquitectura hexagonal con ArchUnit.
- GitHub Actions ejecuta `mvn verify`, PostgreSQL Testcontainers y la barrera JaCoCo en cada push a `main` y pull request.

## Seguridad y operación

- Bean Validation en el borde HTTP y validación defensiva dentro del dominio.
- Consultas parametrizadas por Spring Data/JPA; no se concatena SQL de usuario.
- Rangos de disponibilidad y tamaños de página acotados antes de ejecutar consultas costosas.
- Mensajes 500 sin trazas ni detalles internos.
- Restricciones de integridad, llaves foráneas, optimistic locking e índices por médico/paciente/fecha.
- open-in-view deshabilitado.
- `.dockerignore` usa una lista permitida y solo envía el Dockerfile y el JAR al contexto de construcción.
- Temurin está fijado a Java `21.0.9+10` y digest SHA-256; PostgreSQL de Compose y Testcontainers también usa digest inmutable.
- La API se ejecuta como UID/GID `10001`, con filesystem de solo lectura, `/tmp` efímero, sin capabilities y con `no-new-privileges`.
- El perfil PostgreSQL no inicia sin `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`; `.env` y archivos de secretos están ignorados por Git.
- Dependabot revisa semanalmente Maven, Dockerfile, Docker Compose y GitHub Actions.
- Dependency Review analiza cada pull request y bloquea dependencias nuevas con vulnerabilidades `high` o `critical`.
- No se implementa autenticación/autorización porque fue excluida explícitamente del alcance.

La automatización sigue la configuración recomendada por la
[documentación oficial de Dependency Review](https://docs.github.com/en/code-security/how-tos/secure-your-supply-chain/manage-your-dependency-security/configure-dependency-review-action)
y las [actualizaciones de versión de Dependabot](https://docs.github.com/en/code-security/how-tos/secure-your-supply-chain/secure-your-dependencies/configure-version-updates).

## Evolución y despliegue

La entrega no depende de una URL pública en nube. El `Dockerfile` usa una imagen JRE 21 inmutable y sin
root, y Compose ofrece PostgreSQL; el mismo artefacto puede publicarse posteriormente en Azure Container Apps,
AWS App Runner, Google Cloud Run, Render o Railway. Para producción se recomienda además:

- proveedor administrado de PostgreSQL y secretos en el gestor de la nube;
- autenticación OIDC/JWT y autorización por rol;
- observabilidad con Actuator/OpenTelemetry;
- idempotency keys para clientes móviles;
- outbox/eventos para recordatorios;
- calendario clínico por médico, sede y zona horaria.
