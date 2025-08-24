# Movie Backend - Spring Boot Microservices 🎬

A modern microservices architecture for managing a movie database built with Spring Boot 3 and PostgreSQL. Features JWT authentication, Spring Cloud Gateway routing, and automated database migrations.

## 🏗️ Architecture

- **Gateway Service** (8080) - Spring Cloud Gateway for request routing and CORS
- **Movie Service** (8081) - Movie CRUD operations with PostgreSQL database  
- **Auth Service** (8082) - JWT-based authentication and user management

## 🔧 Prerequisites

- **Java 17+**
- **Maven 3.6+** 
- **PostgreSQL** (any recent version)
- **Git CLI**

## 🚀 Quick Start

### 1. Clone & Build
```bash
git clone https://github.com/skuknuraknu/springboot-movies.git
cd springboot-movies
./mvnw clean install
```

### 2. Setup Configuration
Copy example configurations and update credentials:
```bash
# Copy configuration templates
cp gateway/src/main/resources/application.yml.example gateway/src/main/resources/application.yml
cp movie-service/src/main/resources/application.yml.example movie-service/src/main/resources/application.yml  
cp auth-service/src/main/resources/application.yml.example auth-service/src/main/resources/application.yml

# Update database credentials in each application.yml file
# Update JWT secret in auth-service/src/main/resources/application.yml
```

### 3. Setup Database & Migrate
```bash
# Create PostgreSQL database named 'movieapp'
# Run migrations for all services
./mvnw flyway:migrate
```

### 4. Start Services
```bash
# Start in separate terminals or background
./mvnw spring-boot:run -pl gateway      # Port 8080 - API Gateway
./mvnw spring-boot:run -pl movie-service # Port 8081 - Movie APIs  
./mvnw spring-boot:run -pl auth-service  # Port 8082 - Auth APIs
```

**Access**: All APIs available through Gateway at `http://localhost:8080`

## 📡 API Endpoints

All requests go through the Gateway at `http://localhost:8080`:

- **Movies**: `/api/movies/**` → Movie Service (CRUD operations)
- **Authentication**: `/api/auth/**` → Auth Service (login, register, JWT)

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.5.4 with Spring Cloud Gateway
- **Database**: PostgreSQL with Flyway migrations  
- **Security**: Spring Security + JWT (JJWT library)
- **Build**: Maven multi-module project
- **Testing**: JUnit Jupiter

## 📁 Project Structure

```
movie-backend/
├── gateway/           # Spring Cloud Gateway (Port 8080)
├── movie-service/     # Movie CRUD APIs (Port 8081) 
├── auth-service/      # Authentication APIs (Port 8082)
├── pom.xml           # Parent POM with shared dependencies
```


