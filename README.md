# Movie App API 🎬

Spring Boot microservice for managing a movie database. This application provides a full suite of CRUD (Create, Read, Update, Delete) operations through a RESTful API and uses **Flyway** for seamless database schema management and version control.

---

## 🔧 Prerequisites

Before you begin, ensure you have the following installed on your system:

-   **Java:** Version `17` or higher.
-   **Maven:** Version `3.6+` or higher.
-   **Git CLI:** For cloning and managing the source code.
-   **PostgreSQL:** Version `17` or higher, running and accessible.

---

## 🚀 Installation & Setup

Follow these steps to get the application running on your local machine.

### 1. Clone the Repository

Open your terminal and clone the project repository:

```bash
git clone https://github.com/skuknuraknu/springboot-movies.git
cd springboot-movies
```

### 2. Build the Project

Use the Maven wrapper to compile the source code and download dependencies:

```bash
./mvnw clean install
```

### 3. Configure Local Environment for Movie Service

You need to set up your database connection properties for your local environment.

-   Copy the local properties template to create your own configuration file:
    ```bash
    cp movie-service/src/main/resources/application-local.properties.example movie-service/src/main/resources/application.properties
    ```
-   Now, open `movie-service/src/main/resources/application.properties` and update the following datasource properties with your PostgreSQL credentials:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/your_database_name
    spring.datasource.username=your_postgres_username
    spring.datasource.password=your_postgres_password
    ```

### 4. Run Database Migrations

With the configuration in place, run the Flyway migrations to set up the database schema:

```bash
./mvnw flyway:migrate
```

### 5. Run the Application

Finally, start the Spring Boot application:

```bash
./mvnw spring-boot:run -pl gateway
./mvnw spring-boot:run -pl movie-service
```

The application will be running on `http://localhost:8080`.

---

## 🔌 API Endpoints

The primary endpoint for interacting with movie data is `/api/movies`.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/movies` | Retrieves a list of all movies. |
| `GET` | `/api/movies/{id}` | Retrieves a single movie by its ID. |
| `POST` | `/api/movies` | Creates a new movie. |
| `DELETE`| `/api/movies/{id}` | Deletes a movie by its ID. |
