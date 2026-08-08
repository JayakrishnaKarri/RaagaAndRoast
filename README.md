# 🍵 RaagaAndRoast - Portfolio-Grade Café Ordering Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-9.5-blue.svg)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A modern, production-grade café ordering and management platform built with **Spring Boot**, demonstrating advanced Java backend engineering skills expected from a 4+ years experienced developer.

## 🎯 Project Overview

RaagaAndRoast is a comprehensive backend application that showcases:

- **Clean Architecture** with proper separation of concerns
- **Advanced Spring Boot** features and best practices
- **Production-ready security** with JWT authentication
- **Complex database relationships** with JPA/Hibernate
- **RESTful API design** with comprehensive documentation
- **Performance optimization** and N+1 problem prevention
- **Comprehensive testing** strategy
- **Enterprise-grade** error handling and validation

## ✨ Key Features

### 🔐 Authentication & Authorization
- **JWT-based authentication** with refresh tokens
- **Role-based access control** (CUSTOMER, STAFF, MANAGER, ADMIN)
- **Permission-based authorization** for fine-grained control
- **Resource ownership** validation
- **BCrypt password hashing**

### 🍽️ Menu Management
- **Category-based organization** with hierarchical structure
- **Dynamic filtering** by dietary preferences (vegetarian, vegan, gluten-free)
- **Price range filtering** and availability status
- **Search functionality** with pagination and sorting
- **Image and metadata** support

### 🛒 Shopping Cart
- **Session-based cart management** for authenticated users
- **Real-time price updates** and availability checking
- **Quantity management** with validation
- **Cart persistence** across sessions
- **Price change notifications**

### 📦 Order Management
- **Complete order lifecycle** (PENDING → CONFIRMED → PREPARING → READY → COMPLETED)
- **Order status tracking** with timestamps
- **Historical price preservation** for audit trails
- **Delivery address management**
- **Order cancellation** with business rules

### 👥 Customer Management
- **Profile management** with address book
- **Order history** and preferences
- **Marketing consent** tracking
- **Birthday and demographic** analytics

## 🏗️ Technical Architecture

### Technology Stack
- **Java 21** - Latest LTS with modern features
- **Spring Boot 4.1.0** - Enterprise application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer
- **Hibernate** - ORM with performance optimizations
- **MySQL 9.5** - Relational database
- **Flyway** - Database migration management
- **Maven** - Build and dependency management
- **OpenAPI/Swagger** - API documentation

### Design Patterns
- **Repository Pattern** - Data access abstraction
- **Service Layer Pattern** - Business logic encapsulation
- **DTO Pattern** - Data transfer and validation
- **Builder Pattern** - Complex object construction
- **Strategy Pattern** - Algorithm encapsulation

### Database Design
```
Users ←→ Roles ←→ Permissions
  ↓
Customers ←→ Addresses
  ↓
Carts ←→ CartItems ←→ MenuItems ←→ Categories
  ↓
Orders ←→ OrderItems
```

## 🚀 Getting Started

### Prerequisites
- **Java 21** or higher
- **Maven 3.9+**
- **MySQL 8.0+**
- **Git**

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/RaagaAndRoast.git
   cd RaagaAndRoast
   ```

2. **Configure database**
   ```bash
   mysql -u root -p
   CREATE DATABASE raaga_and_roast;
   ```

3. **Update application properties**
   ```properties
   # src/main/resources/application.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/raaga_and_roast
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

4. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Access the application**
   - **API Base URL**: http://localhost:8080
   - **Swagger UI**: http://localhost:8080/swagger-ui.html
   - **API Docs**: http://localhost:8080/api-docs
   - **Health Check**: http://localhost:8080/actuator/health

## 📚 API Documentation

### Authentication Endpoints
```http
POST /api/auth/register    # User registration
POST /api/auth/login       # User login
POST /api/auth/refresh     # Token refresh
```

### Core Business Endpoints
```http
# Categories
GET    /api/categories              # List categories
POST   /api/categories              # Create category (MANAGER+)
GET    /api/categories/{id}         # Get category
PUT    /api/categories/{id}         # Update category (MANAGER+)
DELETE /api/categories/{id}         # Delete category (MANAGER+)

# Menu Items
GET    /api/menu-items              # List menu items with filtering
POST   /api/menu-items              # Create menu item (MANAGER+)
GET    /api/menu-items/{id}         # Get menu item
PUT    /api/menu-items/{id}         # Update menu item (MANAGER+)
DELETE /api/menu-items/{id}         # Delete menu item (MANAGER+)

# Shopping Cart
GET    /api/cart                    # Get current user's cart
POST   /api/cart/items              # Add item to cart
PATCH  /api/cart/items/{id}         # Update cart item
DELETE /api/cart/items/{id}         # Remove cart item
DELETE /api/cart/items              # Clear cart

# Orders
GET    /api/orders                  # List user's orders
POST   /api/orders                  # Create order
GET    /api/orders/{id}             # Get order details
PATCH  /api/orders/{id}/status      # Update order status (STAFF+)
```

### Advanced Features
- **Pagination**: `?page=0&size=20&sort=name,asc`
- **Filtering**: `?category=beverages&vegetarian=true&minPrice=5.00`
- **Search**: `?search=coffee&available=true`

## 🔒 Security Features

### JWT Authentication
```bash
# Login to get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"user@example.com","password":"password"}'

# Use token in subsequent requests
curl -H "Authorization: Bearer <your-jwt-token>" \
  http://localhost:8080/api/menu-items
```

### Role-Based Access Control
- **CUSTOMER**: Browse menu, manage cart, place orders
- **STAFF**: View and update order status
- **MANAGER**: Manage menu items and categories
- **ADMIN**: Full system access including user management

## 🧪 Testing

### Run Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn test -Dtest=**/*IntegrationTest

# Test coverage report
mvn jacoco:report
```

### Test Categories
- **Unit Tests**: Service layer business logic
- **Repository Tests**: Data access layer with @DataJpaTest
- **Controller Tests**: Web layer with @WebMvcTest
- **Integration Tests**: End-to-end scenarios with @SpringBootTest
- **Security Tests**: Authentication and authorization

## 📊 Performance Optimizations

### JPA/Hibernate Optimizations
- **Lazy Loading**: Proper relationship configuration
- **Entity Graphs**: Solving N+1 queries
- **Query Optimization**: Custom JPQL and native queries
- **Connection Pooling**: HikariCP configuration
- **Batch Processing**: Bulk operations

### Caching Strategy
- **Application-level caching** for frequently accessed data
- **Query result caching** for expensive operations
- **HTTP caching headers** for API responses

## 🔧 Configuration

### Environment Profiles
- **dev**: Development with debug logging
- **test**: Testing with in-memory database
- **prod**: Production with optimized settings

### Key Configuration Files
- `application.properties` - Main configuration
- `application-dev.properties` - Development overrides
- `application-prod.properties` - Production settings

## 📈 Monitoring & Observability

### Actuator Endpoints
- `/actuator/health` - Application health status
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information

### Logging
- **Structured logging** with Logback
- **Different log levels** per environment
- **Security event logging**
- **Performance monitoring**

## 🚀 Deployment

### Docker Support
```dockerfile
# Dockerfile included for containerization
docker build -t raaga-and-roast .
docker run -p 8080:8080 raaga-and-roast
```

### Production Considerations
- **Environment variables** for sensitive configuration
- **Database connection pooling** optimization
- **JVM tuning** for performance
- **Security headers** configuration
- **Rate limiting** implementation

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Your Name**
- GitHub: [@JayakrishnaKarri](https://github.com/JayakrishnaKarri)
- LinkedIn: [Your LinkedIn](https://linkedin.com/in/yourprofile)
- Email: jayakrishnakarri07@gmail.com

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Hibernate team for the powerful ORM
- MySQL team for the reliable database
- Open source community for inspiration and tools

---

**⭐ If you found this project helpful, please give it a star!**

## 📋 Interview Talking Points

This project demonstrates several key concepts that are valuable in technical interviews:

### Architecture & Design
- **Why microservices vs monolith?** Started with monolith for faster development
- **How would you scale this?** Database sharding, caching, load balancing
- **Security considerations?** JWT, HTTPS, input validation, SQL injection prevention

### Database Design
- **Why these relationships?** Business domain modeling
- **How to handle high traffic?** Read replicas, connection pooling, caching
- **Data consistency?** Transactions, optimistic locking, eventual consistency

### Performance
- **N+1 problem solutions** Entity graphs, batch fetching, query optimization
- **Caching strategy** Application cache, database query cache, CDN
- **Monitoring approach** Metrics, logging, health checks, alerting

### Testing Strategy
- **Test pyramid** Unit tests (fast), integration tests (realistic), E2E tests (complete)
- **Test data management** Fixtures, factories, database cleanup
- **Mocking strategy** When to mock vs real dependencies
