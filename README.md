> 🇦🇷 README también disponible en [español](README.es.md)

# 📚 Library Management API — V2

A RESTful API for managing a library system built with **Spring Boot**. Supports full CRUD operations for books, genres, and libraries, as well as a complete **loan management system** with user authentication and role-based access control.  
Built using a classic layered architecture (Controller → Service → Repository → Database), with DTO-based data transfer, centralized exception handling, paginated endpoints, JWT-based security, and scheduled tasks — ensuring clean separation of concerns, scalability, and testability. Fully tested with JUnit and Mockito.

---

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8 |
| Security | Spring Security + JWT |
| Validation | Spring Validation (Jakarta) |
| Documentation | SpringDoc OpenAPI (Swagger UI) |
| Containerization | Docker / Docker Compose |
| Build Tool | Maven |
| Utilities | Lombok |

---

## 📦 Domain Model

```
Library ──< Book >── Genre
              │
              └──< Loan >── User
```

- A **Library** has many **Books**
- A **Book** belongs to one **Library** and has many **Genres**
- A **User** can create **Loans** to borrow books
- A **Loan** tracks borrow date, due date, return date, and status (`ACTIVE`, `RETURNED`, `OVERDUE`)
- A scheduled task runs daily at midnight to automatically mark overdue loans

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

## 🔐 Authentication & Authorization

The API uses **JWT (JSON Web Token)** for stateless authentication. Include the token in the `Authorization` header for protected endpoints:

```
Authorization: Bearer <your_token>
```

### Roles

| Role | Description |
|---|---|
| `ADMIN` | Full access — can create, update, delete books, genres, libraries, and view all loans |
| `CLIENT` | Can create loans, return their own books, and view their own loan history |

### Public Endpoints (no authentication required)

- `GET /api/books/**`, `GET /api/genres/**`, `GET /api/libraries/**`
- `POST /api/auth/register`, `POST /api/auth/login`
- Swagger UI (`/swagger-ui/**`, `/v3/api-docs/**`)

---

## 📖 API Reference

Interactive docs available at: `http://localhost:8080/swagger-ui.html`

### Authentication `/api/auth`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user | Public |
| `POST` | `/api/auth/login` | Log in and receive a JWT token | Public |

#### Example — Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "secret123"
}
```

#### Example — Login Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### Books `/api/books`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/books` | Create a book | `ADMIN` |
| `POST` | `/api/books/import` | Import a book from Google Books by ISBN | `ADMIN` |
| `GET` | `/api/books` | Get all books (paginated) | Public |
| `GET` | `/api/books/{id}` | Get book by ID | Public |
| `GET` | `/api/books/available` | Get available books (paginated) | Public |
| `GET` | `/api/books/external/{isbn}` | Search book info on Google Books by ISBN | Public |
| `PUT` | `/api/books/{id}` | Update a book | `ADMIN` |
| `DELETE` | `/api/books/{id}` | Delete a book | `ADMIN` |
| `PATCH` | `/api/books/{id}/genres/{genreId}` | Add a genre to a book | `ADMIN` |
| `DELETE` | `/api/books/{id}/genres/{genreId}` | Remove a genre from a book | `ADMIN` |

#### Example — Create Book

```http
POST /api/books
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "title": "1984",
  "author": "George Orwell",
  "synopsis": "A dystopian social science fiction novel.",
  "libraryId": 1,
  "genreIds": [1, 2]
}
```

#### Example — Import Book from Google Books

```http
POST /api/books/import
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "isbn": "9780451524935",
  "stock": 5,
  "libraryId": 1,
  "genreIds": [1]
}
```

#### Example — Book Response

```json
{
  "id": 1,
  "title": "1984",
  "author": "George Orwell",
  "synopsis": "A dystopian social science fiction novel.",
  "libraryName": "Central Library",
  "genreNames": ["Fiction", "Dystopia"]
}
```

---

### Genres `/api/genres`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/genres` | Create a genre | `ADMIN` |
| `GET` | `/api/genres` | Get all genres (paginated) | Public |
| `GET` | `/api/genres/{id}` | Get genre by ID | Public |
| `PUT` | `/api/genres/{id}` | Update a genre | `ADMIN` |
| `DELETE` | `/api/genres/{id}` | Delete a genre | `ADMIN` |

---

### Libraries `/api/libraries`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/libraries` | Create a library | `ADMIN` |
| `GET` | `/api/libraries` | Get all libraries (paginated) | Public |
| `GET` | `/api/libraries/{id}` | Get library by ID | Public |
| `PUT` | `/api/libraries/{id}` | Update a library | `ADMIN` |
| `DELETE` | `/api/libraries/{id}` | Delete a library | `ADMIN` |

---

### Loans `/api/loans`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/loans` | Create a loan (borrow a book) | `ADMIN`, `CLIENT` |
| `GET` | `/api/loans/me` | Get the current user's loans | `ADMIN`, `CLIENT` |
| `GET` | `/api/loans` | Get all loans (paginated) | `ADMIN` |
| `PATCH` | `/api/loans/{id}/return` | Return a loan | `ADMIN`, `CLIENT` |

#### Example — Create Loan

```http
POST /api/loans
Authorization: Bearer <client_token>
Content-Type: application/json

{
  "bookId": 1
}
```

#### Example — Loan Response

```json
{
  "id": 1,
  "bookTitle": "1984",
  "userEmail": "john@example.com",
  "loanDate": "2025-03-01",
  "dueDate": "2025-03-15",
  "returnDate": null,
  "status": "ACTIVE"
}
```

---

## ⏰ Scheduled Tasks

| Task | Schedule | Description |
|---|---|---|
| Overdue loan checker | Daily at midnight (`0 0 0 * * *`) | Automatically marks `ACTIVE` loans past their due date as `OVERDUE` |

---

## 👤 Author

**Matias Roman Diaz**  
GitHub: [matiassddiaz](https://github.com/matiassddiaz)