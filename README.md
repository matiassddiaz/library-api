> 🇦🇷 README también disponible en [español](README.es.md)

# 📚 Library Management API

A RESTful API for managing a library system built with **Spring Boot**. Supports full CRUD operations for books, genres, and libraries, including book rental management.  
Built using a classic layered architecture (Controller → Service → Repository → Database), with DTO-based data transfer, centralized exception handling, and paginated endpoints to ensure clean separation of concerns, scalability, and testability. Fully tested with JUnit and Mockito.

---

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8 |
| Validation | Spring Validation (Jakarta) |
| Documentation | SpringDoc OpenAPI (Swagger UI) |
| Containerization | Docker / Docker Compose |
| Build Tool | Maven |
| Utilities | Lombok |

---

## 📦 Domain Model

```
Library ──< Book >── Genre
```

- A **Library** has many **Books**
- A **Book** belongs to one **Library** and has many **Genres**
- A **Book** can be rented or returned

---


## 🚀 Getting Started

### Prerequisites

- **Docker & Docker Compose** 

### Run with Docker Compose

```bash
git clone https://github.com/matiassddiaz/library-api.git
cd library-api
docker-compose up --build
```
The API will be available at `http://localhost:8080`.

---

## 📖 API Reference

Interactive docs available at: `http://localhost:8080/swagger-ui.html`

### Books `/api/books`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/books` | Create a book |
| `GET` | `/api/books` | Get all books (paginated) |
| `GET` | `/api/books/{id}` | Get book by ID |
| `GET` | `/api/books/available` | Get available (non-rented) books (paginated) |
| `PUT` | `/api/books/{id}` | Update a book |
| `DELETE` | `/api/books/{id}` | Delete a book |
| `PATCH` | `/api/books/{id}/rent` | Rent a book |
| `PATCH` | `/api/books/{id}/return` | Return a book |
| `PATCH` | `/api/books/{id}/genres/{genreId}` | Add a genre to a book |
| `DELETE` | `/api/books/{id}/genres/{genreId}` | Remove a genre from a book |

### Genres `/api/genres`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/genres` | Create a genre |
| `GET` | `/api/genres` | Get all genres |
| `GET` | `/api/genres/{id}` | Get genre by ID |
| `PUT` | `/api/genres/{id}` | Update a genre |
| `DELETE` | `/api/genres/{id}` | Delete a genre |

### Libraries `/api/libraries`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/libraries` | Create a library |
| `GET` | `/api/libraries` | Get all libraries |
| `GET` | `/api/libraries/{id}` | Get library by ID |
| `PUT` | `/api/libraries/{id}` | Update a library |
| `DELETE` | `/api/libraries/{id}` | Delete a library |

---

### Example Request — Create Book

```http
POST /api/books
Content-Type: application/json

{
  "title": "1984",
  "author": "George Orwell",
  "synopsis": "A dystopian social science fiction novel.",
  "libraryId": 1,
  "genreIds": [1, 2]
}
```

### Example Response

```json
{
  "id": 1,
  "title": "1984",
  "author": "George Orwell",
  "synopsis": "A dystopian social science fiction novel.",
  "rented": false,
  "libraryName": "Central Library",
  "genreNames": ["Fiction", "Dystopia"]
}
```

---


## 👤 Author

**Matias Roman Diaz**  
GitHub: [matiassddiaz](https://github.com/matiassddiaz)
