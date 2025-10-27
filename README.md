# E-Commerce Platform with JWT Authentication

## Technology Stack

- **Backend Framework**: Spring Boot 3.5.7
- **Security**: Spring Security with JWT
- **Database**: MySQL
- **ORM**: Spring Data JPA (Hibernate)
- **Build Tool**: Maven
- **Java Version**: 17
- **API Documentation**: Swagger/OpenAPI 3.0
- **Libraries**:
  - JJWT 0.11.5 for JWT handling
  - Lombok for reducing boilerplate
  - Spring Boot Actuator for monitoring

---

## API Endpoints

### Public Endpoints (No Authentication Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/public/register` | Register a new user (default: CONSUMER) |
| POST | `/api/public/login` | Login and receive JWT token |
| GET | `/api/public/product/search?keyword={name}` | Search products by name or category |
| GET | `/api/public/listProductsMenu` | List all available products |

### Consumer Endpoints (Requires JWT with ROLE_CONSUMER)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/auth/consumer/cart` | Get consumer's cart with products |
| POST | `/api/auth/consumer/cart` | Add product to cart |
| PUT | `/api/auth/consumer/cart` | Update product quantity in cart |
| DELETE | `/api/auth/consumer/cart` | Remove product from cart |

### Seller Endpoints (Requires JWT with ROLE_SELLER)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/auth/seller/product` | Get all products by seller |
| GET | `/api/auth/seller/product/{productId}` | Get specific product details |
| POST | `/api/auth/seller/product` | Create new product |
| PUT | `/api/auth/seller/product` | Update product (by name) |
| DELETE | `/api/auth/seller/product/{productId}` | Delete product |

### Admin Endpoints (Requires JWT with ROLE_ADMIN)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/auth/admin/consumers` | List all consumers |
| GET | `/api/auth/admin/sellers` | List all sellers |
| DELETE | `/api/auth/admin/consumer/{username}` | Delete a consumer |
| DELETE | `/api/auth/admin/seller/{username}` | Delete a seller |

### Swagger Documentation
- **URL**: `http://localhost:8080/api/swagger-ui/index.html`

---

## Setup Instructions

### Prerequisites
- Java 17
- Maven 3.6+
- MySQL 8.0+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Database Setup
```sql
CREATE DATABASE jwt_db;
```

### Configuration
Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jwt_db
spring.datasource.username=root
spring.datasource.password=your_password
```

### Running the Application
```bash
# Using Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

Application will start on: `http://localhost:8080/api`

(Visualize a step-by-step wizard: Check off prerequisites, auto-generate config files, or simulate run commands!)

---

### Common HTTP Status Codes
- `200 OK` - Request successful
- `201 Created` - Resource created
- `400 Bad Request` - Invalid input
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (e.g., duplicate cart item)
