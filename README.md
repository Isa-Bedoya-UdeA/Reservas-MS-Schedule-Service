# Plataforma de Reservas de Servicios - MS-Schedule-Service

[![CI/CD Pipeline](https://github.com/Isa-Bedoya-UdeA/Reservas-MS-Schedule-Service/actions/workflows/build.yml/badge.svg)](https://github.com/Isa-Bedoya-UdeA/Reservas-MS-Schedule-Service/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Isa-Bedoya-UdeA_Reservas-MS-Schedule-Service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Isa-Bedoya-UdeA_Reservas-MS-Schedule-Service)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Isa-Bedoya-UdeA_Reservas-MS-Schedule-Service&metric=bugs)](https://sonarcloud.io/summary/new_code?id=Isa-Bedoya-UdeA_Reservas-MS-Schedule-Service)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=Isa-Bedoya-UdeA_Reservas-MS-Schedule-Service&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=Isa-Bedoya-UdeA_Reservas-MS-Schedule-Service)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Isa-Bedoya-UdeA_Reservas-MS-Schedule-Service&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Isa-Bedoya-UdeA_Reservas-MS-Schedule-Service)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=Isa-Bedoya-UdeA_Reservas-MS-Schedule-Service&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=Isa-Bedoya-UdeA_Reservas-MS-Schedule-Service)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=Isa-Bedoya-UdeA_Reservas-MS-Schedule-Service&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=Isa-Bedoya-UdeA_Reservas-MS-Schedule-Service)

## Descripción

CodeF@ctory - Caso 15 - Plataforma de Reservas de Servicios - Microservicio de Horarios y Empleados.

## Responsabilidad

Gestión de empleados, asignación de servicios a empleados, horarios laborales recurrentes y bloqueos de horario específicos por fecha para reservas.

## Tecnologías

### Backend

* **Java 17**
* **Spring Boot 3.5.14**
* **Spring Security** (Autenticación y autorización)
* **Spring Data JPA** (Persistencia)
* **JWT** (JSON Web Tokens para autenticación)
* **MapStruct** (Mapeo entre entidades y DTOs)
* **Lombok** (Reducción de código boilerplate)
* **Maven** (Gestión de dependencias)

### Herramientas de Desarrollo

* **Git** (Control de versiones)
* **GitHub** (Repositorio remoto)
* **Postman** (Pruebas de APIs)
* **SonarCloud** (Análisis de calidad de código)

## Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

* **JDK 17** o superior
* **Maven 3.8+**
* **Oracle Database** o **PostgreSQL**
* **Git**

## Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/Isa-Bedoya-UdeA/Reservas-MS-Schedule-Service
cd Reservas-MS-Schedule-Service
```

### 2. Configurar la Base de Datos y Propiedades

Copia el archivo `.env.example` a `.env`:

```bash
cp .env.example .env
```

Edita el archivo `.env` con tus credenciales de Supabase:

```bash
# SPRING PROFILE
SPRING_PROFILE=dev

# DATABASE CONFIG - SUPABASE (Transaction Pooler - IPv4 compatible)
DB_URL=jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0
DB_USER=postgres.[TU-PROJECT-REF]
DB_PASSWORD=[TU-CONTRASEÑA-DE-SUPABASE]

# EXTERNAL SERVICES URLs
SERVICES_AUTH_URL=http://localhost:8081
```

### 3. Configurar JWT

Genera un JWT_SECRET seguro:

```bash
openssl rand -base64 64
```

Agrega el JWT_SECRET generado a tu archivo `.env`:

```bash
JWT_SECRET=[TU-JWT-SECRET-SEGURA]
JWT_EXPIRATION=86400000
```

> **IMPORTANTE:** El JWT_SECRET debe ser el mismo en todos los microservicios.

### 4. Compilar el Proyecto

```bash
mvn clean install -U -DskipTests 
```

### 5. Ejecutar la Aplicación

```bash
# Opción 1:
mvn spring-boot:run

# Opción 2:
mvn clean spring-boot:run

# Si falla al ejecutar, lanzar este comando, y luego nuevamente run
taskkill /F /IM java.exe 2>$null
```

La aplicación estará disponible en: `http://localhost:8083`

## Estructura del Proyecto

```
Reservas-MS-Schedule-Service/
├── src/
│   ├── main/
│   │   ├── java/com/codefactory/reservasmsresourceservice/
│   │   │   ├── client/              # Feign Clients para comunicación entre microservicios
│   │   │   ├── config/              # Configuración de Spring (Security, JWT, CORS, etc.)
│   │   │   ├── controller/          # Controladores REST (Employee, Health)
│   │   │   ├── dto/                 # Data Transfer Objects (Request y Response)
│   │   │   ├── entity/              # Entidades JPA (Employee, EmployeeServiceOffering, WorkSchedule, ScheduleBlock)
│   │   │   ├── exception/           # Excepciones personalizadas y manejo global
│   │   │   ├── mapper/              # Mapeadores (MapStruct) entre entidades y DTOs
│   │   │   ├── repository/          # Repositorios Spring Data JPA
│   │   │   ├── security/            # Seguridad (JWT filter, JWT service)
│   │   │   ├── service/             # Interfaces de servicios
│   │   │   └── service/impl/        # Implementaciones de servicios
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── application-test.properties
│   └── test/
│       ├── java/                    # Tests unitarios y de integración
│       └── resources/
├── docs/                            # Documentación y pruebas
├── .env.example                     # Plantilla de variables de entorno
├── .env                             # Variables de entorno (no versionado)
├── pom.xml                          # Configuración de Maven
└── README.md
```

## Endpoints Principales

### Health Check
- `GET /api/`: Health Check - Retorna estado del servicio
- `GET /api/version`: Version Check - Retorna versión del servicio

### Empleados
- `POST /api/schedule/employees`: Crear empleado (requiere ROLE_PROVEEDOR) - El providerId se obtiene del JWT
- `PUT /api/schedule/employees/{id}`: Actualizar empleado existente (requiere ROLE_PROVEEDOR) - Solo el proveedor creador puede modificar
- `DELETE /api/schedule/employees/{id}`: Eliminar empleado (hard delete) (requiere ROLE_PROVEEDOR) - Solo el proveedor creador puede eliminar
- `PATCH /api/schedule/employees/{id}/deactivate`: Desactivar empleado (soft delete) (requiere ROLE_PROVEEDOR) - Solo el proveedor creador puede desactivar
- `PATCH /api/schedule/employees/{id}/activate`: Activar empleado (requiere ROLE_PROVEEDOR) - Solo el proveedor creador puede activar
- `GET /api/schedule/employees/{id}`: Obtener empleado por ID (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño puede ver
- `GET /api/schedule/employees`: Listar todos los empleados del proveedor autenticado (requiere ROLE_PROVEEDOR)
- `GET /api/schedule/employees/active`: Listar solo empleados activos de todos los proveedores (Público)

### Asociación Empleado-Servicio (Employee Service Offerings)
- `POST /api/schedule/employee-services`: Asociar empleado a servicio (requiere ROLE_PROVEEDOR) - El proveedor debe ser dueño del empleado y del servicio
- `PATCH /api/schedule/employee-services/{id}/deactivate`: Desactivar asociación (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño puede desactivar
- `PATCH /api/schedule/employee-services/{id}/activate`: Activar asociación (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño puede activar
- `DELETE /api/schedule/employee-services/{id}`: Eliminar asociación (hard delete) (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño puede eliminar
- `GET /api/schedule/employee-services/service/{serviceId}`: Listar empleados de un servicio (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño del servicio puede ver
- `GET /api/schedule/employee-services/employee/{employeeId}`: Listar servicios de un empleado (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño del empleado puede ver
- `GET /api/schedule/employee-services/service/{serviceId}/active`: Listar empleados activos de un servicio (Público) - Cualquier usuario puede ver (para reservas)

### Horarios Laborales (Work Schedules)
- `POST /api/schedule/work-schedules`: Crear horario laboral recurrente (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño del empleado puede crear
- `PUT /api/schedule/work-schedules/{id}`: Actualizar horario laboral existente (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño del empleado puede modificar
- `DELETE /api/schedule/work-schedules/{id}`: Eliminar horario laboral (hard delete) (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño del empleado puede eliminar
- `GET /api/schedule/work-schedules/{id}`: Obtener horario laboral por ID (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño del empleado puede ver
- `GET /api/schedule/work-schedules/employee/{employeeId}`: Listar todos los horarios de un empleado (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño puede ver
- `GET /api/schedule/work-schedules/employee/{employeeId}/active`: Listar horarios activos de un empleado (requiere autenticación) - Cualquier usuario autenticado puede ver
- `GET /api/schedule/work-schedules/employee/{employeeId}/public`: Listar horarios de un empleado (requiere autenticación) - Cualquier usuario autenticado puede ver

### Bloqueos de Horario (Schedule Blocks)
- `POST /api/schedule/schedule-blocks`: Crear bloqueo de horario por fecha específica (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño del empleado puede crear
- `DELETE /api/schedule/schedule-blocks/{id}`: Eliminar bloqueo de horario (soft delete) (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño del empleado puede eliminar
- `GET /api/schedule/schedule-blocks/{id}`: Obtener bloqueo de horario por ID (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño del empleado puede ver
- `GET /api/schedule/schedule-blocks/employee/{employeeId}`: Listar todos los bloqueos de un empleado (requiere ROLE_PROVEEDOR) - Solo el proveedor dueño puede ver
- `GET /api/schedule/schedule-blocks/employee/{employeeId}/public`: Listar bloqueos de un empleado (requiere autenticación) - Cualquier usuario autenticado puede ver
- `GET /api/schedule/schedule-blocks/employee/{employeeId}/date-range`: Listar bloqueos en rango de fechas (requiere autenticación)
- `GET /api/schedule/schedule-blocks/employee/{employeeId}/date`: Listar bloqueos en fecha específica (requiere autenticación)
- `POST /api/schedule/schedule-blocks/reservation`: Crear bloqueo para reserva (interno, usado por MS-Reservation)
- `DELETE /api/schedule/schedule-blocks/reservation/{reservationId}`: Cancelar bloqueo de reserva (interno, usado por MS-Reservation)
- `GET /api/schedule/schedule-blocks/check-availability`: Verificar disponibilidad de empleado (requiere autenticación)

## Relaciones entre Entidades

- **Employee**: Entidad que representa empleados (recurso humano) asociados a proveedores
- **EmployeeServiceOffering**: Entidad que representa la relación N:N entre empleados y servicios (un empleado puede ofrecer múltiples servicios)
- **WorkSchedule**: Entidad que representa los horarios laborales recurrentes de los empleados (ej: todos los lunes 8am-2pm)
- **ScheduleBlock**: Entidad que representa bloqueos de horario por fecha específica (reservas, vacaciones, permisos, etc.)

## Diagramas

### Diseño Arquitectonico
![Diseño Arquitectonico](docs/architecture.png)

### Diagrama de Paquetes y Componentes
![Paquetes y Componentes](docs/components.png)

### Diagrama MER Lógico
![MER Lógico](docs/mer-diagram.png)

### Diagrama de despliegue
![Diagrama de despliegue](docs/deployment-diagram.png)

## Documentación de API (Swagger/OpenAPI)
![Swagger](docs/swagger.png)

**Ruta de acceso:** http://localhost:8083/swagger-ui/index.html#/

## Variables de Entorno para Despliegue

Para configurar el despliegue del microservicio, consulta la documentación detallada de variables de entorno:

**[docs/environment-variables.md](docs/environment-variables.md)**

También puedes usar el archivo de ejemplo como plantilla:
**[.env.example](.env.example)** - Copia este archivo a `.env` y configura tus valores.

## Secrets para Kubernetes

Para el despliegue en Kubernetes, consulta la documentación de secrets:

**[docs/kubernetes-secrets.md](docs/kubernetes-secrets.md)**

## Observabilidad: Kubernetes, ArgoCD, Prometheus y Grafana

**[docs/observability.md](docs/observability.md)** — Guía paso a paso para desplegar y monitorear el microservicio.

## HATEOAS

**[docs/hateoas.md](docs/hateoas.md)** — Implementación de HATEOAS (Hypermedia as the Engine of Application State) con enlaces `_links` en todas las respuestas REST para navegación discoverable.

## Pruebas en Postman

Para ver las pruebas detalladas de la API, consulta el archivo [docs/PruebasPostman.md](docs/PruebasPostman.md).