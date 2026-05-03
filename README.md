# Product Service

Spring Boot service for managing products and publishing events when a product is created.

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
- Unit tests (service layer)
- Controller tests
- Integration tests

---

## Docker Setup

This project uses Docker Compose to run external dependencies.

### Prerequisites

- Docker Desktop installed and running

---

## Running Locally

### 1. Start dependencies

```docker compose up -d```

This starts:
- PostgreSQL (port 5432)
- Kafka (port 9092)

---

### 2. Run the application

bash:

```./mvnw spring-boot:run```

powershell:

```.\mvnw.cmd spring-boot:run```

App runs on:

http://localhost:8080

---

## API

### Create product

POST /products
Content-Type: application/json
```
{
"name": "Product name",
"price": 12345.67
}
```
Response:

```
{
"id": "uuid",
"name": "Product name",
"price": 12345.67,
"createdAt": "2026-05-02T12:25:49Z"
}
```

---

### Get product by ID

```GET /products/{id}```

---

### List products

```GET /products?page=0&size=20```

---

## Kafka

Topic:

*product.created*

Example message:
```
{
"productId": "uuid",
"name": "Product name",
"price": 12345.67,
"occurredAt": "2026-05-02T12:54:57Z"
}
```
Consume messages from Kafka:

bash:
```
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



## Database

PostgreSQL connection (default values for local development):


```jdbc:postgresql://localhost:5432/productdb```


username: productuser (default)

password: productpass (default)


Table is created automatically via JPA (ddl-auto=update).

---

## Configuration

The application uses environment variables with default values defined in application.yml.

Examples:
```
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/productdb}
    username: ${DB_USERNAME:productuser}
    password: ${DB_PASSWORD:productpass}

  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

### Overriding configuration

You can override these values by setting environment variables.

Example (bash):

```
export DB_URL=jdbc:postgresql://prod-db:5432/productdb
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export KAFKA_BOOTSTRAP_SERVERS=prod-kafka:9092
```

```./mvnw spring-boot:run```

Example (powershell):

```
$env:DB_URL="jdbc:postgresql://prod-db:5432/productdb"
$env:DB_USERNAME="prod_user"
$env:DB_PASSWORD="secure_password"
$env:KAFKA_BOOTSTRAP_SERVERS="prod-kafka:9092"
```

```.\mvnw.cmd spring-boot:run```

### Production note

Default values are intended for local development only.
In a production environment, all sensitive configuration (database credentials, Kafka endpoints...) should be provided via environment variables or a secure configuration system.

---

## Testing

Run all tests:

```./mvnw test```

Windows:

```.\mvnw.cmd test```

### Test types

- Unit tests (service layer)
- Controller tests (@WebMvcTest)
- Integration tests:
  - PostgreSQL Testcontainer
  - Kafka Testcontainer
  - Full flow: HTTP -> DB -> Kafka

---

## Security

Authentication and authorization are intentionally out of scope for this MVP.
In a production environment, the service would be protected using OAuth2/JWT or integrated with the platform's existing identity provider.


---

## Logging

Application logs are written to:

```logs/product-service.log```


Log levels:
- Root: WARN
- Application (`com.smg.productservice`) and spring: INFO

---

## Design Notes

- Product names are not unique in this MVP. In a real system, uniqueness would be enforced via a business identifier such as SKU or product code.
- Consumers should be idempotent since the system provides at-least-once delivery.
- Mapping is implemented manually for simplicity. In larger systems, MapStruct can be used.
- 
