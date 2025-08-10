# 📝 Notes API (Spring Boot + Redis)

A simple REST API to create, retrieve, list, and delete notes using **Spring Boot** and **Redis** for fast, in-memory data storage.

---

## 🚀 Features
- Create notes with `title` and `content`
- Retrieve a single note by its `id`
- Retrieve all notes
- Delete notes
- Data stored in **Redis** (Hash structure) for fast access
- API documented using **Swagger/OpenAPI**

---

## 🛠 Tech Stack
- **Java 17+**
- **Spring Boot** (Web, Data Redis, Validation)
- **Redis**
- **Lombok**
- **Swagger/OpenAPI**

---

## 📦 Getting Started

### 1️⃣ Prerequisites
- [Java 17+](https://adoptopenjdk.net/)
- [Maven](https://maven.apache.org/)
- [Redis](https://redis.io/)

---

### 2️⃣ Run Redis

How it Works
1. ADD NOTE

POST /notes
{
  "title": "Meeting Notes",
  "content": "Discuss project"
}

Response:
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Meeting Notes",
  "content": "Discuss project"
}

Test with cURL:
-- Create a Note: 
curl -X POST http://localhost:8080/api/notes \
-H "Content-Type: application/json" \
-d '{"title": "Shopping list", "content": "Buy milk and bread"}'

-- Get all notes:
curl -X GET http://localhost:8080/api/notes

-- Get note by ID:
curl -X GET http://localhost:8080/api/notes/{id}

-- Delete Note
curl -X DELETE http://localhost:8080/api/notes/{id}





