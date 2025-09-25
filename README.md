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

## 🧪 Testing Strategy

Our testing approach is comprehensive and follows different testing levels:

### Unit Tests
- **Controller Tests**: Verify request/response handling and input validation
- **Service Tests**: Ensure business logic works correctly in isolation
- **Repository Tests**: Validate database operations
- **Mapper Tests**: Verify DTO-Entity conversions

### Integration Tests
- **API Integration**: Full request-response cycle testing
- **Database Integration**: Verify JPA operations with H2
- **Scheduler Integration**: Test automatic status updates

### Test Categories

#### 1. Todo Item Lifecycle
- Creation with different states
- Status transitions (NOT_DONE → DONE)
- Past due handling
- Immutability of PAST_DUE items

#### 2. Error Handling
- Invalid input validation
- Database operation failures
- Concurrent update scenarios
- Past due update failures

#### 3. Edge Cases
- Items with exact due times
- Multiple status transitions
- Boundary conditions
- Null/empty values

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "TodoBackendApplicationTests"

# Run specific test method
./gradlew test --tests "TodoBackendApplicationTests.given_pastDueItem_when_attemptingModifications_then_allModificationsAreRejected"
```

## 📝 API Behavior

### Todo Item States

Items can be in one of three states:
- `NOT_DONE`: Initial state of new items
- `DONE`: Manually marked as completed
- `PAST_DUE`: Automatically set when due date passes

### State Transitions

1. `NOT_DONE` → `DONE`: Manual update via API
2. `NOT_DONE` → `PAST_DUE`: Automatic update by scheduler
3. `DONE` stays `DONE`: Cannot transition to other states
4. `PAST_DUE` is immutable: Cannot be modified

### Key Features

#### Automatic Past Due Detection
- Scheduler runs every minute
- Checks for items past their due date
- Updates status to PAST_DUE if applicable
- Immutable once marked as PAST_DUE

#### Immutability Rules
1. PAST_DUE items:
   - Cannot update description
   - Cannot change status
   - Cannot modify due date
   - All modification attempts return 400 Bad Request

2. DONE items:
   - Cannot be unmarked
   - Status is permanent

### API Endpoints Detail

#### Create Todo Item
- **Endpoint**: `POST /api/todos`
- **Status**: Always created as `NOT_DONE`
- **Validation**:
  - Description: Required, max 1000 chars
  - Due Date: Optional

#### Update Todo Item
- **Endpoint**: `PATCH /api/todos/{id}`
- **Restrictions**:
  - Cannot modify PAST_DUE items
  - Cannot unmark DONE items
- **Fields**: description, status

#### List Todo Items
- **Endpoint**: `GET /api/todos`
- **Filters**:
  - `?status=done`
  - `?status=not_done`
  - `?status=past_due`
- **Ordering**: By creation time (descending)

### Error Handling

The API uses standard HTTP status codes:
- `400 Bad Request`: Invalid input or state transition
- `404 Not Found`: Item doesn't exist
- `500 Internal Server Error`: Database or system errors

Example error response:
```json
{
    "status": 400,
    "message": "Cannot modify PAST_DUE item",
    "timestamp": "2025-09-25T22:51:51.037801"
}
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
├── main/
│   ├── java/com/sidpaw/todobackend/
│   │   ├── TodoBackendApplication.java
│   │   ├── config/
│   │   │   └── SwaggerConfig.java
│   │   ├── controller/
│   │   │   ├── PingController.java
│   │   │   └── TodoController.java
│   │   ├── dto/
│   │   │   ├── TodoPatchDTO.java
│   │   │   ├── TodoRequestDTO.java
│   │   │   └── TodoResponseDTO.java
│   │   ├── entity/
│   │   │   └── TodoItemEntity.java
│   │   ├── mapper/
│   │   │   └── TodoItemMapper.java
│   │   ├── model/
│   │   │   └── TodoStatus.java
│   │   ├── repository/
│   │   │   └── TodoItemRepository.java
│   │   ├── scheduler/
│   │   │   └── TodoItemScheduler.java
│   │   └── service/
│   │       └── TodoItemService.java
│   └── resources/
│       └── application.properties
└── test/
    ├── java/com/sidpaw/todobackend/
    │   ├── TodoBackendApplicationTests.java
    │   ├── controller/
    │   │   ├── PingControllerTest.java
    │   │   ├── PingControllerUnitTest.java
    │   │   └── TodoControllerTest.java
    │   ├── mapper/
    │   │   └── TodoItemMapperTest.java
    │   ├── repository/
    │   │   └── TodoItemRepositoryTest.java
    │   ├── scheduler/
    │   │   └── TodoItemSchedulerTest.java
    │   └── service/
    │       ├── TodoItemServiceIntegrationTest.java
    │       └── TodoItemServiceUnitTest.java
    └── resources/
        ├── application-test.properties
        └── cleanup.sql
```

## 👨‍💻 Author

**Siddharth Pawar** - [pawar.siddharth04@gmail.com](mailto:pawar.siddharth04@gmail.com)
