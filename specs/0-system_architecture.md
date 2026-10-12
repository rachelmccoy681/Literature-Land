# Technical Architecture

## Microservice Architecture
This application contains four **Microservice Modules**:
- **Cart service**
- **Customer service**
- **Order service**
- **Payment service**


## Layered Architecture
Each microservice module is structured into four distinct layers:
1. **Presentation Layer** (Controllers)
2. **Service Layer** (Business Logic)
3. **Domain Layer** (Core Domain Models & Rules)
4. **Data Access Layer** (Repositories)

## Repository Structure 
The project is structured as a multi-module repository:
- `book-service`: Module for the Book Service
- `library-service`: Module for the Library Service

## Technology Stack
- **JDK & Build**: Java 21, Apache Maven (Multi-module POM)
- **Framework**: Spring Boot
    - **Controller Layer**: `@RestController` 
    - **Service Layer**: `@Service`
    - **Domain Layer**: `@Entity`
    - **Data Access Layer**: `@Repository`
- **Database**: H2 in-memory database for development
- **Unit & Integration Testing**: `MockMvc`, `@SpringBootTest`, `JUnit 5`

## Design Principles
Ensure the system is scalable and extendable as it is being built, such that making future changes/adding additional features will not require changes to the system architecture.