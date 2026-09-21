# PulsePass

PulsePass es un caso de estudio académico para la capa de persistencia de una plataforma de eventos. El proyecto modela el dominio de venues, eventos, artistas, usuarios, perfiles y tickets usando Java 21, Spring Boot 4, Spring Data JPA, Hibernate, PostgreSQL y Flyway.

## Objetivo del proyecto

Implementar un modelo relacional robusto que cumpla los requisitos del PRD, preservando integridad de datos y validando el comportamiento real con pruebas de integración contra PostgreSQL en contenedores Docker mediante Testcontainers.

## Stack tecnológico

- Java 21
- Spring Boot 4.1.1
- Spring Data JPA
- Hibernate ORM
- PostgreSQL
- Flyway
- Maven
- Testcontainers

## Alcance

Este proyecto está centrado en la persistencia y la lógica de datos. No incluye:

- Frontend
- API REST completa
- Autenticación/autorización
- Pasarela de pagos
- Notificaciones o emails
- Gestión administrativa avanzada

## Modelo de dominio

El proyecto cubre las siguientes entidades principales:

- Venue
- Event
- Artist
- User
- UserProfile
- Ticket

Además, incluye los enums principales:

- EventCategory
- EventStatus
- TicketType
- TicketStatus

## Reglas de negocio implementadas

- Códigos únicos de negocio
- Restricciones de integridad en PostgreSQL
- Validación de capacidad, precios y estados válidos
- Relación 1:N entre venue y eventos
- Relación N:M entre eventos y artistas
- Relación 1:1 entre usuario y perfil
- Relación 1:N entre usuario y tickets
- Relación 1:N entre evento y tickets

## Estructura del proyecto

```text
pulsepass/
├── src/
│   ├── main/
│   │   ├── java/com/pulsepass/pulsepass/
│   │   │   ├── domain/
│   │   │   ├── repository/
│   │   │   └── PulsepassApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/
│   └── test/java/com/pulsepass/pulsepass/
│       ├── PersistenceIntegrationTest.java
│       ├── TestcontainersConfiguration.java
│       └── ...
├── pom.xml
├── mvnw
├── mvnw.cmd
├── PRD_PulsePass.md
└── README.md
```

## Requisitos previos

### Java

- JDK 21 instalado
- Variable JAVA_HOME configurada

### Maven

- Maven disponible en el sistema o usar el wrapper del proyecto

### Docker

- Docker Desktop o Docker Engine activo
- Requerido para Testcontainers cuando se ejecutan las pruebas de integración

## Configuración de base de datos

El proyecto está configurado con PostgreSQL usando variables de entorno por defecto:

- URL: jdbc:postgresql://localhost:5432/pulsepass
- Usuario: postgres
- Contraseña: postgres

La configuración está en:

- src/main/resources/application.properties

## Ejecutar pruebas

Desde la raíz del proyecto:

```bash
./mvnw test
```

En Windows PowerShell:

```powershell
./mvnw.cmd test
```

## Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
./mvnw.cmd spring-boot:run
```

## Levantar PostgreSQL con Docker (opcional para ejecución local)

```bash
docker run --name pulsepass-db -e POSTGRES_DB=pulsepass -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16
```

## Migraciones con Flyway

Las migraciones se encuentran en:

- src/main/resources/db/migration/

Se ejecutan automáticamente al iniciar la aplicación y en pruebas de integración.

## Verificación realizada

Se validó la aplicación con la suite real del proyecto usando Maven y el resultado fue exitoso:

```text
EXIT:0
```

Esto evidencia que la configuración actual y las pruebas de integración no fallan en el entorno configurado.

## Estado del proyecto

El proyecto se encuentra en estado funcional para la capa de persistencia y cumple con la lógica principal del PRD, especialmente en:

- persistencia JPA
- migraciones Flyway
- relaciones entre entidades
- validación de restricciones y unicidad
- pruebas reales con PostgreSQL mediante Testcontainers

## Referencias importantes

- PRD del proyecto: PRD_PulsePass.md
- Código principal: src/main/java/com/pulsepass/pulsepass
- Pruebas: src/test/java/com/pulsepass/pulsepass

## URL del repositorio
- https://github.com/nromero-4/pulsepass

## URL del branch
- https://github.com/nromero-4/pulsepass/tree/feature/artists

## Nota final

Este proyecto está orientado a demostrar la construcción de un modelo de persistencia realista y verificable en PostgreSQL, con foco en integridad, trazabilidad y pruebas de integración, siguiendo el enfoque académico del caso de estudio.
