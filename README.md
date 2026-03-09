# E-Commerce Backend System (Spring Boot)

A RESTful backend service for an e-commerce platform. It provides a foundational API for user authentication, product catalog management, cart handling, order processing, and payment integration.


## Core Features

*   **Authentication & Authorization:** JWT-based stateless authentication with Role-Based Access Control (RBAC).

*   **Concurrency & Data Integrity:** Utilizes full `@Transactional` management alongside explicit Optimistic and Pessimistic Locking to ensure strict data consistency during simultaneous order processing.

*   **Distributed Caching:** Redis used for product catalog caching to reduce database load and improve read performance.
    
*   **Abuse Prevention:** API rate limiting implemented via Bucket4j and Redis.
  
*   **Global Exception Handling:** Centralized `@RestControllerAdvice` ensuring consistent RESTful error responses.
  
*   **Payment Processing:** Asynchronous Stripe Webhook handling for reliable payment state management.
    
*   **Observability & Logging:** Centralized logging with SLF4J and Logback including structured request logs and filtered Spring framework traces.

## CI/CD


**Automated Builds & Tests: Maven builds and tests run automatically on every push or pull request.

**Security Scanning: CodeQL analyzes the code for potential vulnerabilities.

**Dockerized Deployment: The application is packaged as a Docker image and pushed to Docker Hub automatically.

**Versioned Images: Each commit is tagged with its unique SHA, preserving image history and enabling rollbacks.

**Pipeline Tool: GitHub Actions orchestrates the CI/CD workflow.





## Architecture Overview

The project follows a domain-oriented, layered architecture. Code is organized by business domains for clarity and maintainability, while separating concerns across application, domain, and infrastructure layers.


```text
src/main/java/com/example/e_commerce
├── security/    # Authentication filters, JwtUtils, and Auth Controllers
├── user/        # User entity, Data Access, and Account Services
├── product/     # Products, Categories, and associated DTOs
├── cart/        # Session cart calculation and temporary persistence
├── order/       # Concurrency handling and Order State Management
├── payment/     # Stripe Integration and Asynchronous Webhooks
├── exception/   # Global Exception Advisories
└── config/      # Redis, Security, and foundational Beans
```

## Technology Stack

*   **Language:** Java 21
*   **Framework:** Spring Boot 3.x
*   **Data & Persistence:** MySQL, Spring Data JPA (Hibernate)
*   **Performance & Caching:** Redis
*   **Security:** Spring Security, JSON Web Tokens (JJWT)
*   **External Integrations:** Stripe Java API
*   **Build & Tooling:** Maven, MapStruct, Lombok, springdoc-openapi (Swagger UI)

## Run Locally

### Steps

**1. Clone**
```bash
git clone https://github.com/0xalit/e-commerce.git
cd e-commerce
```

**2. Configure Environment**
Duplicate `.env.example` as `.env` and fill in the required variables (DB credentials, Stripe keys, JWT secret).

**3. Start Dependencies (Database & Redis)**
```bash
docker-compose up -d db redis
```

**4. Build all modules**
```bash
./mvnw clean install
```

**5. Start all services**
```bash
./mvnw spring-boot:run
```

*Note: Alternatively, to start everything via Docker:*
```bash
docker-compose up --build
```
*(A pre-built image is available on [Docker Hub](https://hub.docker.com/repository/docker/0xalit/e-commerce-api/general). If you prefer not to build from source, update `docker-compose.yml` image property to `0xalit/e-commerce-api:latest`).*

## API Reference Overview

The API is fully documented using OpenAPI 3. Once the application is running, the interactive Swagger UI can be accessed at:
`http://localhost:8080/swagger-ui/index.html`

*Note: While many endpoints (like viewing products and categories) are public, protected routes require an `Authorization: Bearer <token>` header.*

| Domain | Base Path | Scope / Capabilities |
| :--- | :--- | :--- |
| **Authentication** | `/api/auth` | User registration, stateless login, and JWT generation |
| **Users** | `/api/users` | Profile retrieval and role assignment (RBAC constrained) |
| **Products** | `/api/products` | Catalog search, pagination, and inventory CRUD |
| **Categories** | `/api/categories` | Product grouping and taxonomy management |
| **Cart** | `/api/cart` | Cart management with item calculation and persistence. |
| **Orders** | `/api/orders` | Checkout execution and order history tracking |
| **Payments** | `/api/payments` | Stripe PaymentIntent creation |
| **Webhooks** | `/api/webhooks` | Public endpoint for handling asynchronous Stripe events |
