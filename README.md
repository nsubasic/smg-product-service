# Product Service

Simple Spring Boot service for managing products and publishing events when a product is created.

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Kafka
- Testcontainers

---

## Features

- Create product (name, price)
- Get product by ID
- List products (paginated)
- Validation (name not empty, price > 0)
- Publish `product.created` event to Kafka
- Unit tests for service and controller tests
- Integration tests

---
## Docker Setup

This project uses Docker Compose to run external dependencies.

### Prerequisites

- Docker Desktop installed and running
---
## Running Locally

### 1. Start dependencies

```bash
docker compose up -d
```

This starts:
- PostgreSQL (port 5432)
- Kafka (port 9092)

---

### 2. Run the application

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

App runs on:

```
http://localhost:8080
```

---

## API

### Create product

```http
POST /products
Content-Type: application/json
```

```json
{
  "name": "Product name",
  "price": 12345.67
}
```

Response:

```json
{
  "id": "uuid",
  "name": "Product name",
  "price": 12345.67,
  "createdAt": "2026-05-02T12:25:49Z"
}
```

---

### Get product by ID

```http
GET /products/{id}
```

---

### List products

```http
GET /products?page=0&size=20
```

---

## Kafka

Topic:

```
product.created
```

Example message:

```json
{
  "productId": "uuid",
  "name": "Product name",
  "price": 12345.67,
  "occurredAt": "2026-05-02T12:54:57Z"
}
```

Consume messages:

bash:

```bash
docker exec -it product-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic product.created \
  --from-beginning
```

powershell:
```
docker exec -it product-kafka /opt/kafka/bin/kafka-console-consumer.sh `
--bootstrap-server localhost:9092 `
--topic product.created `
--from-beginning
```
---

## Database

PostgreSQL connection:

```
jdbc:postgresql://localhost:5432/productdb
username: productuser
password: productpass
```

Table is created automatically via JPA (`ddl-auto=update`).

---

## Testing

Run all tests:

```bash
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```

### Test types

- Unit tests (service layer)
- Controller tests (`@WebMvcTest`)
- Integration tests:
  - PostgreSQL Testcontainer
  - Kafka Testcontainer
  - Full flow: HTTP -> DB -> Kafka

---
## Security

Authentication and authorization are intentionally out of scope for this MVP. In a production environment, the service would be protected using OAuth2/JWT or integrated with the platform's existing identity provider.