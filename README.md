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
- Unit, controller, and integration tests

---

## Architecture

```
Controller → Service → Repository → PostgreSQL
                       ↓
                    Kafka Producer → product.created topic
```

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
  "name": "BMW 320d",
  "price": 25000
}
```

Response:

```json
{
  "id": "uuid",
  "name": "BMW 320d",
  "price": 25000,
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
  "name": "BMW 320d",
  "price": 25000,
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
    - Full flow: HTTP → DB → Kafka

---

## Design Decisions

- No Flyway (schema handled via JPA for MVP simplicity)
- Event-driven approach using Kafka
- Validation at API layer
- Testcontainers for realistic integration testing

---

## Possible Improvements

- Outbox pattern for guaranteed event delivery
- Idempotency for create operations
- Retry + DLQ for Kafka failures
- OpenAPI / Swagger documentation
- Authentication / authorization
- Dockerized application container

---

## Notes

This project focuses on:
- clean architecture
- realistic integration testing
- event-driven communication