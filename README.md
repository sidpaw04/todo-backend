# todo-backend

# Todo List Service

A simple and efficient Spring Boot backend service for managing todo items with comprehensive health monitoring and API documentation.

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **Spring Boot Actuator**
- **Spring Validation**
- **H2 Database**
- **MapStruct** for DTO mapping
- **Swagger/OpenAPI 3**
- **JUnit 5** with Mockito
- **Docker** for containerization

## 🏃‍♂️ Quick Start

### Prerequisites
- Java 21+ (for local development)
- Docker (for containerized deployment)
- Git

### Running Locally

```bash
# Clone the repository
git clone https://gitlab.com/SiddharthPawar/todo-list-service.git
cd todo-list-service

# Run the application
./gradlew bootRun
```

### Running with Docker

#### Option 1: Docker Compose (Recommended)
```bash
# Start the application
docker compose up

# Run in detached mode (background)
docker compose up -d

# View logs
docker compose logs -f

# Stop the application
docker compose down
```

#### Option 2: Plain Docker
```bash
# Build the Docker image
docker build -t todo-list-service .

# Run the container
docker run -p 8080:8080 todo-list-service

# Run in detached mode (background)
docker run -d -p 8080:8080 --name todo-service todo-list-service

# View logs
docker logs todo-service

# Stop the container
docker stop todo-service
```

The application will be available at `http://localhost:8080`

### Key Endpoints

#### Health & Documentation
- **Health Check**: `http://localhost:8080/actuator/health`
- **API Documentation**: `http://localhost:8080/swagger-ui.html`
- **Ping**: `http://localhost:8080/api/ping`

#### Todo Management
- **Create Todo**: `POST /api/todos` - Create a new todo item
- **Get All Todos**: `GET /api/todos` - Retrieve all todo items
- **Get Todo by ID**: `GET /api/todos/{id}` - Get a specific todo item
- **Get Todos by Status**: `GET /api/todos/status/{status}` - Filter by status (NOT_DONE, DONE, PAST_DUE)
- **Mark as Done**: `PUT /api/todos/{id}/done` - Mark a todo item as completed
- **Get Statistics**: `GET /api/todos/stats` - Get todo counts by status

## 🧪 Testing

### Local Testing
```bash
# Run all tests
./gradlew test

# Run specific test classes
./gradlew test --tests "*PingController*"
```

### Docker Testing
```bash
# Test with Docker Compose
docker compose up -d
curl http://localhost:8080/actuator/health
docker compose down

# Or test the Docker build directly
docker build -t todo-list-service .
docker run --rm -p 8080:8080 todo-list-service
```

## 🔧 Configuration

The application uses H2 in-memory database by default. Configuration can be modified in:
- `src/main/resources/application.properties`
- `src/test/resources/application-test.properties` (for tests)

## 📖 Documentation

- **Swagger UI**: Visit `http://localhost:8080/swagger-ui.html` when the application is running
- **OpenAPI Spec**: Available at `http://localhost:8080/api-docs`

## 🏗️ Project Structure

```
src/
├── main/java/com/sidpaw/todolistservice/
│   ├── TodoListServiceApplication.java
│   ├── config/
│   │   └── SwaggerConfig.java
│   └── controller/
│       └── PingController.java
└── test/java/com/sidpaw/todolistservice/
    ├── controller/
    │   ├── PingControllerTest.java
    │   └── PingControllerUnitTest.java
    └── integration/
        └── PingControllerIntegrationTest.java
```

## 👨‍💻 Author

**Siddharth Pawar** - [pawar.siddharth04@gmail.com](mailto:pawar.siddharth04@gmail.com)
