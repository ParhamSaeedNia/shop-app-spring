# E-Commerce Backend API

A comprehensive e-commerce backend built with Spring Boot, following Domain-Driven Design (DDD) and MVC principles. This API provides complete functionality for user management, product catalog, shopping cart, order processing, and payment handling.

## 🏗️ Architecture Overview

The backend follows a layered architecture with clear separation of concerns:

- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic and use cases
- **Repository Layer**: Manages data access using Spring Data JPA
- **Domain Layer**: Represents core entities and business rules

## 🚀 Features

### Authentication & Authorization

- JWT-based authentication
- OAuth2 integration (Google, Facebook)
- OTP-based mobile verification
- Role-based access control (ADMIN, CUSTOMER)

### User Management

- User registration and login
- Profile management
- Password change functionality
- Address management

### Product & Category Management

- CRUD operations for products and categories
- Product search and filtering
- Price range filtering
- Stock management
- Category-based product organization

### Shopping Cart

- Add/remove items from cart
- Update quantities
- Real-time price calculation
- Cart persistence

### Order Management

- Order creation from cart
- Order status tracking
- Order history
- Order cancellation

### Payment Processing

- Multiple payment methods
- Payment gateway integration
- Payment status tracking
- Refund processing

### Inventory Management

- Stock tracking
- Low stock notifications
- Automatic stock updates

## 🛠️ Technologies Used

- **Spring Boot 3.5.7**
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data access layer
- **H2 Database** - In-memory database for development
- **PostgreSQL** - Production database
- **JWT** - Token-based authentication
- **OAuth2** - Social login integration
- **Lombok** - Boilerplate code reduction
- **MapStruct** - DTO mapping
- **Redis** - Caching (optional)
- **Maven** - Dependency management

## 📋 Prerequisites

- Java 25 or higher
- Maven 3.6 or higher
- PostgreSQL (for production)
- Redis (optional, for caching)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd shop-app
```

### 2. Configure Database

Update `src/main/resources/application.properties` with your database configuration:

```properties
# For development (H2)
spring.datasource.url=jdbc:h2:mem:shopdb
spring.datasource.username=sa
spring.datasource.password=password

# For production (PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/shopdb
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Configure OAuth2 (Optional)

Update OAuth2 client credentials in `application.properties`:

```properties
spring.security.oauth2.client.registration.google.client-id=your-google-client-id
spring.security.oauth2.client.registration.google.client-secret=your-google-client-secret
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### 5. Access H2 Console (Development)

Visit `http://localhost:8080/h2-console` to access the H2 database console.

## 📚 API Documentation

### Base URL

```
http://localhost:8080/api
```

### Authentication Endpoints

#### Register User

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "phoneNumber": "+1234567890"
}
```

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "john@example.com",
  "password": "password123"
}
```

#### Get Current User

```http
GET /api/auth/me
Authorization: Bearer <access_token>
```

### OTP Endpoints

#### Send OTP

```http
POST /api/auth/otp/send
Content-Type: application/json

{
  "phoneNumber": "+1234567890"
}
```

#### Verify OTP

```http
POST /api/auth/otp/verify
Content-Type: application/json

{
  "phoneNumber": "+1234567890",
  "otpCode": "123456"
}
```

### Product Endpoints

#### Get All Products

```http
GET /api/products?page=0&size=10&sortBy=name&sortDir=asc
```

#### Get Product by ID

```http
GET /api/products/{id}
```

#### Search Products

```http
GET /api/products/search?keyword=laptop&page=0&size=10
```

#### Get Products by Category

```http
GET /api/products/category/{categoryId}?page=0&size=10
```

#### Get Products by Price Range

```http
GET /api/products/price-range?minPrice=100&maxPrice=500&page=0&size=10
```

### Category Endpoints

#### Get All Categories

```http
GET /api/categories
```

#### Get Category by ID

```http
GET /api/categories/{id}
```

#### Search Categories

```http
GET /api/categories/search?name=electronics
```

### Cart Endpoints

#### Get Current User Cart

```http
GET /api/cart
Authorization: Bearer <access_token>
```

#### Add Item to Cart

```http
POST /api/cart/add
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

#### Update Cart Item

```http
PUT /api/cart/items/{cartItemId}
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "quantity": 3
}
```

#### Remove Item from Cart

```http
DELETE /api/cart/items/{cartItemId}
Authorization: Bearer <access_token>
```

#### Clear Cart

```http
DELETE /api/cart/clear
Authorization: Bearer <access_token>
```

### Order Endpoints

#### Create Order

```http
POST /api/orders
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "shippingAddress": "123 Main St, City, State 12345",
  "billingAddress": "123 Main St, City, State 12345",
  "notes": "Please deliver after 5 PM"
}
```

#### Get Order by ID

```http
GET /api/orders/{id}
Authorization: Bearer <access_token>
```

#### Get User Orders

```http
GET /api/orders?page=0&size=10&sortBy=createdAt&sortDir=desc
Authorization: Bearer <access_token>
```

#### Get Orders by Status

```http
GET /api/orders/status/PENDING
Authorization: Bearer <access_token>
```

#### Cancel Order

```http
PUT /api/orders/{id}/cancel
Authorization: Bearer <access_token>
```

### Payment Endpoints

#### Process Payment

```http
POST /api/payments
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "orderId": 1,
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "4111111111111111",
  "expiryDate": "12/25",
  "cvv": "123",
  "cardholderName": "John Doe"
}
```

#### Get Payment by Order ID

```http
GET /api/payments/order/{orderId}
Authorization: Bearer <access_token>
```

#### Get User Payments

```http
GET /api/payments
Authorization: Bearer <access_token>
```

#### Refund Payment

```http
POST /api/payments/{paymentId}/refund
Authorization: Bearer <access_token>
```

### Admin Endpoints

#### Create Category

```http
POST /api/admin/categories?name=Electronics&description=Electronic devices
Authorization: Bearer <admin_access_token>
```

#### Update Category

```http
PUT /api/admin/categories/{id}?name=Electronics&description=Updated description
Authorization: Bearer <admin_access_token>
```

#### Delete Category

```http
DELETE /api/admin/categories/{id}
Authorization: Bearer <admin_access_token>
```

#### Get Low Stock Products

```http
GET /api/admin/products/low-stock?threshold=10
Authorization: Bearer <admin_access_token>
```

## 🔒 Security

### JWT Authentication

- Access tokens expire in 24 hours
- Refresh tokens for token renewal
- Stateless authentication

### Role-Based Access Control

- **CUSTOMER**: Can access cart, orders, and profile endpoints
- **ADMIN**: Can access all endpoints including admin functions

### OAuth2 Integration

- Google and Facebook login support
- Automatic user creation for OAuth users

### OTP Verification

- SMS-based verification for mobile numbers
- Rate limiting to prevent abuse
- Configurable expiry time

## 🧪 Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

### Test Coverage

The project includes comprehensive test coverage for:

- Service layer business logic
- Controller endpoints
- Repository data access
- Security configurations

## 🚀 Deployment

### Docker Deployment

```bash
# Build the application
mvn clean package

# Build Docker image
docker build -t shop-app .

# Run container
docker run -p 8080:8080 shop-app
```

### Production Configuration

1. Update database configuration for PostgreSQL
2. Configure Redis for caching
3. Set up proper JWT secrets
4. Configure OAuth2 credentials
5. Set up monitoring and logging

## 📊 Database Schema

### Core Entities

- **User**: User accounts and authentication
- **Product**: Product catalog with categories
- **Category**: Product categorization
- **Cart**: Shopping cart for users
- **CartItem**: Items in shopping cart
- **Order**: Order information
- **OrderItem**: Items in orders
- **Payment**: Payment transactions
- **Shipment**: Delivery tracking
- **Otp**: OTP verification records

### Relationships

- User 1 → N Order
- User 1 → 1 Cart
- Cart 1 → N CartItem
- Order 1 → N OrderItem
- Product N → 1 Category
- Order 1 → 1 Payment
- Order 1 → 1 Shipment

## 🔧 Configuration

### Environment Variables

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/shopdb
DB_USERNAME=your_username
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:

- Create an issue in the repository
- Check the API documentation
- Review the test cases for usage examples

## 🔄 Version History

- **v1.0.0** - Initial release with core e-commerce functionality
- Complete user management
- Product catalog with search and filtering
- Shopping cart functionality
- Order processing
- Payment integration
- Admin panel features
