# Chat-Orchestra

A production-ready API server for managing conversations with RAG integration and automatic summary generation.

## Overview

Chat-Orchestra is a Spring Boot 3.5.5 application that provides a clean API for managing conversations following Pure MSA principles. It focuses solely on conversation storage and management, while clients interact directly with RAG servers for AI responses and summaries. This design ensures loose coupling, service independence, and fault isolation.

## Features

- **Pure Conversation Management**: Start, manage, and end conversation sessions
- **Message Storage**: Store user and assistant messages with timestamps
- **Database Integration**: MySQL with JPA/Hibernate for persistent storage
- **MSA Compliance**: Loose coupling with external services (RAG servers)
- **OpenAPI Documentation**: Swagger UI for API exploration
- **Global Exception Handling**: Consistent error responses
- **CORS Support**: Configured for frontend development
- **Comprehensive Testing**: Unit and integration tests

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.5.5
- **Build Tool**: Gradle (Groovy)
- **Documentation**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, MockMvc
- **Validation**: Bean Validation (Jakarta)

## Quick Start

### Prerequisites

- Java 17 or higher
- Gradle 8.11.1 or higher

### Running the Application

1. **Clone and navigate to the project directory:**
   ```bash
   cd chat-orchestra
   ```

2. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

3. **Access the application:**
   - API Base URL: `http://localhost:8081`
   - Swagger UI: `http://localhost:8081/swagger-ui.html`
   - Health Check: `http://localhost:8081/actuator/health`

## MSA Architecture

### Service Responsibilities

**Chat-Orchestra (This Service):**
- ✅ Conversation session management (start/end)
- ✅ Message storage and retrieval
- ✅ Conversation history tracking

**RAG Server (External Service):**
- ✅ AI response generation (`POST /chat`)
- ✅ Conversation summarization (`POST /conversation/summarize`)

### Client Integration Pattern

```javascript
// 1. Start conversation
const conversation = await fetch('/api/conversations', { method: 'POST' });
const { sessionId } = await conversation.json();

// 2. Save user message
await fetch(`/api/conversations/${sessionId}/messages`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    role: 'USER',
    content: 'Hello, how are you?'
  })
});

// 3. Get AI response from RAG server
const aiResponse = await fetch('http://rag-server:8000/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    message: 'Hello, how are you?',
    sessionId: sessionId
  })
});
const { response } = await aiResponse.json();

// 4. Save AI response
await fetch(`/api/conversations/${sessionId}/messages`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    role: 'ASSISTANT',
    content: response
  })
});

// 5. End conversation
await fetch(`/api/conversations/${sessionId}/end`, {
  method: 'PUT',
  body: JSON.stringify({ reason: 'User ended conversation' })
});

// 6. Get conversation summary from RAG server
const summary = await fetch('http://rag-server:8000/conversation/summarize', {
  method: 'POST',
  body: JSON.stringify({ sessionId, count: 10 })
});
```

## API Endpoints

### Conversation Management

#### 1. Start Conversation
```bash
curl -X POST http://localhost:8081/api/conversation/start \
  -H "Content-Type: application/json" \
  -d '{"user_id":"yeon"}'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "sessionId": "123e4567-e89b-12d3-a456-426614174000",
    "status": "STARTED",
    "startedAt": "2024-01-15T10:30:00Z"
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### 2. Post Message
```bash
curl -X POST http://localhost:8081/api/conversation/{sessionId}/message \
  -H "Content-Type: application/json" \
  -d '{"role":"USER","content":"안녕"}'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "messageId": "123e4567-e89b-12d3-a456-426614174001",
    "sessionId": "123e4567-e89b-12d3-a456-426614174000",
    "role": "USER",
    "content": "안녕",
    "createdAt": "2024-01-15T10:30:15Z",
    "assistantPreview": "This is a mock reply to: 안녕"
  },
  "timestamp": "2024-01-15T10:30:15Z"
}
```

#### 3. Get Conversation
```bash
curl "http://localhost:8081/api/conversation/{sessionId}?page=0&size=50"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "sessionId": "123e4567-e89b-12d3-a456-426614174000",
    "status": "ACTIVE",
    "messages": [
      {
        "messageId": "123e4567-e89b-12d3-a456-426614174001",
        "role": "USER",
        "content": "안녕",
        "createdAt": "2024-01-15T10:30:15Z"
      }
    ],
    "total": 1
  },
  "timestamp": "2024-01-15T10:30:20Z"
}
```

#### 4. End Conversation
```bash
curl -X POST http://localhost:8081/api/conversation/{sessionId}/end \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "sessionId": "123e4567-e89b-12d3-a456-426614174000",
    "status": "ENDED",
    "endedAt": "2024-01-15T10:35:00Z"
  },
  "timestamp": "2024-01-15T10:35:00Z"
}
```

### Speech-to-Text (STT)

```bash
curl -X POST http://localhost:8081/api/stt \
  -H "Content-Type: application/json" \
  -d '{"audio_base64":"QUJDRA==","language":"ko-KR"}'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "transcript": "Transcribed 4 bytes in ko-KR",
    "durationMs": 202,
    "language": "ko-KR"
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Text-to-Speech (TTS)

```bash
curl -X POST http://localhost:8081/api/tts \
  -H "Content-Type: application/json" \
  -d '{"text":"안녕하세요","voice":"neutral"}'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "audioBase64": "QVVESU867J207Iqk7Yq47ZWY7Iqk",
    "voice": "NEUTRAL",
    "language": "ko-KR",
    "estimatedDurationMs": 500
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Search Index Integration

```bash
curl -X POST http://localhost:8081/api/search-index/query \
  -H "Content-Type: application/json" \
  -d '{"query":"세종대왕","top_k":3}'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "query": "세종대왕",
    "results": [
      {
        "id": "doc-1",
        "score": 0.9,
        "snippet": "Mock snippet about '세종대왕' - result 1"
      },
      {
        "id": "doc-2",
        "score": 0.85,
        "snippet": "Mock snippet about '세종대왕' - result 2"
      },
      {
        "id": "doc-3",
        "score": 0.8,
        "snippet": "Mock snippet about '세종대왕' - result 3"
      }
    ]
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Ending Credits

```bash
curl -X POST http://localhost:8081/api/ending-credits \
  -H "Content-Type: application/json" \
  -d '{"session_id":"{sessionId}","include_duration":true}'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "sessionId": "123e4567-e89b-12d3-a456-426614174000",
    "summary": {
      "messages": 7,
      "durationSec": 125
    },
    "credits": [
      {
        "role": "User",
        "name": "You"
      },
      {
        "role": "Assistant",
        "name": "Chat-Orchestra"
      }
    ]
  },
  "timestamp": "2024-01-15T10:35:00Z"
}
```

## Project Structure

```
src/
├── main/
│   ├── java/com/guidely/chatorchestra/
│   │   ├── config/                 # Configuration classes
│   │   │   ├── CorsConfig.java
│   │   │   └── OpenApiConfig.java
│   │   ├── controller/             # REST controllers
│   │   │   ├── ConversationController.java
│   │   │   ├── SttController.java
│   │   │   ├── TtsController.java
│   │   │   ├── SearchIndexController.java
│   │   │   └── EndingCreditsController.java
│   │   ├── dto/                    # Data Transfer Objects
│   │   │   ├── ResponseEnvelope.java
│   │   │   ├── ErrorPayload.java
│   │   │   ├── conversation/
│   │   │   ├── stt/
│   │   │   ├── tts/
│   │   │   ├── search/
│   │   │   └── credits/
│   │   ├── exception/              # Exception handling
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── model/                  # Domain models
│   │   │   ├── enums/
│   │   │   ├── Conversation.java
│   │   │   ├── Message.java
│   │   │   ├── SearchResult.java
│   │   │   └── Credit.java
│   │   ├── repository/             # Data access layer
│   │   │   └── ConversationRepository.java
│   │   ├── service/                # Business logic layer
│   │   │   ├── ConversationService.java
│   │   │   ├── SttService.java
│   │   │   ├── TtsService.java
│   │   │   ├── SearchIndexService.java
│   │   │   └── EndingCreditsService.java
│   │   └── ChatOrchestraApplication.java
│   └── resources/
│       └── application.yml         # Application configuration
└── test/
    └── java/com/guidely/chatorchestra/
        ├── controller/             # Controller tests
        └── service/                # Service tests
```

## Configuration

### Application Properties

The application is configured via `application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: chat-orchestra
  jackson:
    property-naming-strategy: SNAKE_CASE
    default-property-inclusion: NON_NULL

management:
  endpoints:
    web:
      exposure:
        include: "health,info"

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### CORS Configuration

CORS is configured to allow requests from:
- `http://localhost:3000`
- `http://localhost:5173`

## Testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests ConversationControllerTest
```

### Test Coverage

The project includes comprehensive tests for:
- **Controllers**: WebMvcTest for all REST endpoints
- **Services**: Unit tests for business logic
- **Exception Handling**: Global exception handler tests

## Development

### Building the Project

```bash
# Clean and build
./gradlew clean build

# Build without tests
./gradlew build -x test

# Create executable JAR
./gradlew bootJar
```

### Running in Development Mode

```bash
# Run with hot reload (if using IDE)
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## API Response Format

All API responses follow a consistent envelope format:

### Success Response
```json
{
  "success": true,
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": { ... }
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `INVALID_ARGUMENT` | 400 | Invalid argument provided |
| `RESOURCE_NOT_FOUND` | 404 | Requested resource not found |
| `INVALID_STATE` | 400 | Invalid operation for current state |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please contact the Chat-Orchestra team at support@guidely.com.
