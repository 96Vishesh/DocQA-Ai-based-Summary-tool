# DocQA API Documentation - Postman Testing Guide

Base URL: `http://localhost:8080`

---

## 🔐 Authentication APIs

### 1. Register User

Create a new user account.

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/auth/register` |
| **Content-Type** | `application/json` |

**Request Body:**
```json
{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123"
}
```

**Success Response (200):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "john@example.com",
    "name": "John Doe"
}
```

**Error Response (400):**
```json
{
    "message": "Email already registered"
}
```

---

### 2. Login

Authenticate and get JWT token.

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/auth/login` |
| **Content-Type** | `application/json` |

**Request Body:**
```json
{
    "email": "john@example.com",
    "password": "password123"
}
```

**Success Response (200):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "john@example.com",
    "name": "John Doe"
}
```

> **💡 Tip:** Save the `token` as a Postman environment variable `{{auth_token}}` for authenticated requests.

---

## 📄 Document APIs

> **Note:** Include the JWT token in the Authorization header for protected endpoints:
> `Authorization: Bearer {{auth_token}}`

### 3. Upload Document

Upload a PDF, audio (MP3, WAV), or video (MP4, WebM) file.

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/documents/upload` |
| **Content-Type** | `multipart/form-data` |
| **Authorization** | `Bearer {{auth_token}}` |

**Request Body (form-data):**
| Key | Type | Value |
|-----|------|-------|
| `file` | File | Select your PDF/audio/video file |

**Postman Setup:**
1. Go to Body tab → Select "form-data"
2. Add key: `file`
3. Change type dropdown to "File"
4. Select your file

**Success Response (200):**
```json
{
    "id": 1,
    "originalFileName": "document.pdf",
    "type": "PDF",
    "status": "PROCESSING",
    "summary": null,
    "fileSize": 1024567,
    "createdAt": "2024-01-30T12:00:00",
    "processedAt": null
}
```

---

### 4. Get All Documents

Retrieve list of all uploaded documents.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/documents` |
| **Authorization** | `Bearer {{auth_token}}` |

**Success Response (200):**
```json
[
    {
        "id": 1,
        "originalFileName": "document.pdf",
        "type": "PDF",
        "status": "COMPLETED",
        "summary": "This document discusses...",
        "fileSize": 1024567,
        "createdAt": "2024-01-30T12:00:00",
        "processedAt": "2024-01-30T12:01:00"
    },
    {
        "id": 2,
        "originalFileName": "lecture.mp4",
        "type": "VIDEO",
        "status": "COMPLETED",
        "summary": "A video lecture about...",
        "fileSize": 50000000,
        "createdAt": "2024-01-30T13:00:00",
        "processedAt": "2024-01-30T13:05:00"
    }
]
```

---

### 5. Get Single Document

Get details of a specific document.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/documents/{id}` |
| **Authorization** | `Bearer {{auth_token}}` |

**Path Parameters:**
| Parameter | Description |
|-----------|-------------|
| `id` | Document ID (e.g., `1`) |

**Example:** `GET /api/documents/1`

**Success Response (200):**
```json
{
    "id": 1,
    "originalFileName": "document.pdf",
    "type": "PDF",
    "status": "COMPLETED",
    "summary": "This document provides an overview of...",
    "fileSize": 1024567,
    "createdAt": "2024-01-30T12:00:00",
    "processedAt": "2024-01-30T12:01:00"
}
```

---

### 6. Get Documents by Type

Filter documents by type (PDF, AUDIO, VIDEO).

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/documents/type/{type}` |
| **Authorization** | `Bearer {{auth_token}}` |

**Path Parameters:**
| Parameter | Values |
|-----------|--------|
| `type` | `PDF`, `AUDIO`, `VIDEO` |

**Example:** `GET /api/documents/type/PDF`

---

### 7. Get Document Summary

Get AI-generated summary of a document.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/documents/{id}/summary` |
| **Authorization** | `Bearer {{auth_token}}` |

**Success Response (200):**
```json
{
    "documentId": 1,
    "summary": "This document covers the fundamentals of machine learning, including supervised and unsupervised learning algorithms, neural networks, and practical applications in industry."
}
```

---

### 8. Get Timestamps (Audio/Video)

Get timestamped content segments for audio/video files.

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/documents/{id}/timestamps` |
| **Authorization** | `Bearer {{auth_token}}` |

**Success Response (200):**
```json
{
    "documentId": 2,
    "timestamps": [
        {
            "startTime": 0.0,
            "endTime": 45.5,
            "formattedStartTime": "00:00",
            "formattedEndTime": "00:45",
            "topic": "Introduction to the topic.",
            "content": "Introduction to the topic. In this video, we will explore..."
        },
        {
            "startTime": 45.5,
            "endTime": 120.0,
            "formattedStartTime": "00:45",
            "formattedEndTime": "02:00",
            "topic": "Key concepts explained.",
            "content": "Key concepts explained. The main ideas include..."
        }
    ]
}
```

---

### 9. Download/View Document Content

Get the original file content (for display in browser).

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/documents/{id}/content` |
| **Authorization** | `Bearer {{auth_token}}` |

**Response:** Returns the file with appropriate MIME type.

---

### 10. Stream Media

Stream audio/video file (for media player).

| Property | Value |
|----------|-------|
| **Method** | `GET` |
| **URL** | `/api/documents/{id}/stream` |
| **Authorization** | `Bearer {{auth_token}}` |

**Response:** Streams media with `Accept-Ranges: bytes` header.

---

### 11. Delete Document

Delete a document and its associated content.

| Property | Value |
|----------|-------|
| **Method** | `DELETE` |
| **URL** | `/api/documents/{id}` |
| **Authorization** | `Bearer {{auth_token}}` |

**Success Response:** `204 No Content`

---

## 💬 Chat API

### 12. Send Chat Message

Ask questions about an uploaded document. AI will respond with context-aware answers.

| Property | Value |
|----------|-------|
| **Method** | `POST` |
| **URL** | `/api/chat` |
| **Content-Type** | `application/json` |
| **Authorization** | `Bearer {{auth_token}}` |

**Request Body:**
```json
{
    "documentId": 1,
    "message": "What are the main topics covered in this document?",
    "sessionId": "optional-session-id-for-context"
}
```

| Field | Required | Description |
|-------|----------|-------------|
| `documentId` | Yes | ID of the document to query |
| `message` | Yes | Your question |
| `sessionId` | No | Optional session ID for conversation context |

**Success Response (200):**
```json
{
    "response": "The document covers three main topics: 1) Introduction to machine learning concepts, 2) Supervised learning algorithms including linear regression and decision trees, and 3) Practical implementation examples using Python.",
    "sessionId": "abc123-def456",
    "timestamps": [
        {
            "startTime": 30.5,
            "endTime": 45.2,
            "content": "The main topics discussed are...",
            "formattedTime": "00:30"
        }
    ]
}
```

> **Note:** `timestamps` array is populated only for audio/video documents. Use these to create "Play" buttons that jump to relevant portions.

---

## 📋 Postman Collection Setup

### Environment Variables

Create a Postman environment with these variables:

| Variable | Initial Value |
|----------|---------------|
| `base_url` | `http://localhost:8080` |
| `auth_token` | (leave empty, set after login) |

### Auto-set Token After Login

Add this script to the **Tests** tab of the Login/Register requests:

```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("auth_token", jsonData.token);
}
```

### Pre-request Script for Authenticated Requests

Add to each protected request:
- Header: `Authorization: Bearer {{auth_token}}`

---

## 🧪 Testing Workflow

1. **Register** a new user → Token is auto-saved
2. **Upload** a document (PDF/Audio/Video)
3. Wait for processing (check `status` field)
4. **Get summary** of the document
5. **Ask questions** via Chat API
6. For audio/video: **Get timestamps** and test streaming

---

## ⚠️ Error Responses

| Status Code | Description |
|-------------|-------------|
| `400` | Bad Request - Invalid input |
| `401` | Unauthorized - Missing/invalid token |
| `404` | Not Found - Resource doesn't exist |
| `413` | Payload Too Large - File exceeds 100MB |
| `415` | Unsupported Media Type - Invalid file type |
| `500` | Internal Server Error |

**Error Response Format:**
```json
{
    "message": "Error description",
    "timestamp": "2024-01-30T12:00:00",
    "status": 400
}
```
