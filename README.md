# DocQA - AI-Powered Document & Multimedia Q&A Application

An intelligent full-stack web application that allows users to upload PDF documents, audio, and video files, then interact with an AI-powered chatbot to ask questions, get summaries, and navigate to specific timestamps in media files.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)
![Angular](https://img.shields.io/badge/Angular-17-red.svg)

## Features

- 📄 **Document Upload**: Support for PDF, audio (MP3, WAV), and video (MP4, WebM) files
- 🤖 **AI-Powered Q&A**: Ask questions about uploaded documents with context-aware responses
- 📝 **Automatic Summarization**: Generate summaries of document content
- 🎯 **Timestamp Navigation**: Click to play specific portions of audio/video based on chatbot responses
- 🔍 **Semantic Search**: Vector-based search for relevant content chunks
- 🔐 **JWT Authentication**: Secure user authentication
- 📦 **Docker Ready**: Full containerization with Docker Compose

## Tech Stack

### Backend
- **Java 17** with **Spring Boot 3.2**
- **PostgreSQL** for data persistence
- **LangChain4j** for LLM integration
- **OpenAI API** (GPT-4o-mini, Whisper)
- **Apache PDFBox** for PDF extraction
- **Redis** for caching
- **JUnit 5 + Mockito** for testing

### Frontend
- **Angular 17** with **TypeScript**
- **Angular Router** for navigation
- **HttpClient** for API calls
- **Standalone Components** architecture

### Infrastructure
- **Docker & Docker Compose**
- **GitHub Actions** for CI/CD
- **Nginx** for frontend serving

## Prerequisites

- Java 17+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 16 (or use Docker)
- Redis (or use Docker)
- OpenAI API Key

## Quick Start with Docker

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/docqa.git
   cd docqa
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env and add your OpenAI API key
   ```

3. **Start all services**
   ```bash
   docker-compose up --build
   ```

4. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080

## Local Development

### Backend Setup

1. **Navigate to backend directory**
   ```bash
   cd backend
   ```

2. **Configure database** (application.yml or environment variables)
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/docqa
       username: postgres
       password: postgres
   ```

3. **Set OpenAI API key**
   ```bash
   export OPENAI_API_KEY=your-api-key-here
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

### Frontend Setup

1. **Navigate to frontend directory**
   ```bash
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start development server**
   ```bash
   npm run dev
   ```

4. **Open browser at** http://localhost:5173

## API Documentation

### Documents API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/documents/upload` | Upload a PDF, audio, or video file |
| `GET` | `/api/documents` | List all documents |
| `GET` | `/api/documents/{id}` | Get document details |
| `DELETE` | `/api/documents/{id}` | Delete a document |
| `GET` | `/api/documents/{id}/summary` | Get document summary |
| `GET` | `/api/documents/{id}/timestamps` | Get timestamps for audio/video |
| `GET` | `/api/documents/{id}/stream` | Stream media file |

### Chat API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/chat` | Send a message and get AI response |

**Request Body:**
```json
{
  "documentId": 1,
  "message": "What is this document about?",
  "sessionId": "optional-session-id"
}
```

**Response:**
```json
{
  "response": "This document discusses...",
  "sessionId": "generated-session-id",
  "timestamps": [
    {
      "startTime": 30.5,
      "endTime": 45.2,
      "content": "Relevant content...",
      "formattedTime": "00:30"
    }
  ]
}
```

### Authentication API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register new user |
| `POST` | `/api/auth/login` | Login and get JWT token |

## Running Tests

### Backend Tests
```bash
cd backend
mvn clean test
mvn jacoco:report  # Generate coverage report
```
Coverage report available at: `backend/target/site/jacoco/index.html`

### Frontend Tests
```bash
cd frontend
npm test
npm run test:coverage
```

## Project Structure

```
docqa/
├── backend/
│   ├── src/main/java/com/panscience/docqa/
│   │   ├── controller/       # REST controllers
│   │   ├── service/          # Business logic
│   │   ├── entity/           # JPA entities
│   │   ├── repository/       # Data access
│   │   ├── dto/              # Data transfer objects
│   │   ├── config/           # Configuration
│   │   ├── security/         # JWT authentication
│   │   └── exception/        # Exception handling
│   ├── src/test/java/        # Unit & integration tests
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/       # React components
│   │   ├── pages/            # Page components
│   │   ├── services/         # API services
│   │   └── types/            # TypeScript types
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml
├── .github/workflows/ci.yml
└── README.md
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `OPENAI_API_KEY` | OpenAI API key (required) | - |
| `JWT_SECRET` | Secret for JWT signing | auto-generated |
| `DB_HOST` | PostgreSQL host | localhost |
| `DB_PORT` | PostgreSQL port | 5432 |
| `DB_NAME` | Database name | docqa |
| `DB_USERNAME` | Database username | postgres |
| `DB_PASSWORD` | Database password | postgres |
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |

## CI/CD Pipeline

The GitHub Actions pipeline includes:
1. **Backend Tests**: Run Maven tests with PostgreSQL
2. **Coverage Check**: Verify 95%+ test coverage
3. **Frontend Tests**: Run Vitest tests
4. **Docker Build**: Build production images

## License

This project is licensed under the MIT License.

## Author

Vishesh Srivastava
