> 🇺🇸 README also available in [English](README.md)

# 📚 API de Gestión de Biblioteca — V2

Una API RESTful para gestionar un sistema de biblioteca construida con **Spring Boot**. Soporta operaciones CRUD completas para libros, géneros y bibliotecas, además de un **sistema completo de préstamos** con autenticación de usuarios y control de acceso basado en roles.  
Construida utilizando una arquitectura clásica por capas (Controller → Service → Repository → Database), con transferencia de datos basada en DTOs, manejo centralizado de excepciones, endpoints paginados, seguridad con JWT y tareas programadas — garantizando una clara separación de responsabilidades, escalabilidad y capacidad de prueba. Completamente testeada con JUnit y Mockito.

---

## 🧰 Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4 |
| ORM | Spring Data JPA / Hibernate |
| Base de Datos | MySQL 8 |
| Seguridad | Spring Security + JWT |
| Validación | Spring Validation (Jakarta) |
| Documentación | SpringDoc OpenAPI (Swagger UI) |
| Contenedores | Docker / Docker Compose |
| Herramienta de Build | Maven |
| Utilidades | Lombok |

---

## 📦 Modelo de Dominio

```
Library ──< Book >── Genre
              │
              └──< Loan >── User
```

- Una **Biblioteca** tiene muchos **Libros**
- Un **Libro** pertenece a una **Biblioteca** y tiene muchos **Géneros**
- Un **Usuario** puede crear **Préstamos** para tomar libros prestados
- Un **Préstamo** registra la fecha de inicio, fecha de vencimiento, fecha de devolución y estado (`ACTIVE`, `RETURNED`, `OVERDUE`)
- Una tarea programada se ejecuta diariamente a medianoche para marcar automáticamente los préstamos vencidos

---

## 🚀 Cómo Empezar

### Requisitos Previos

- **Docker & Docker Compose**

### Ejecutar con Docker Compose

```bash
git clone https://github.com/matiassddiaz/library-api.git
cd library-api
docker-compose up --build
```

La API estará disponible en `http://localhost:8080`.

---

## 🔐 Autenticación y Autorización

La API utiliza **JWT (JSON Web Token)** para autenticación sin estado. Incluí el token en el header `Authorization` para los endpoints protegidos:

```
Authorization: Bearer <tu_token>
```

### Roles

| Rol | Descripción |
|---|---|
| `ADMIN` | Acceso total — puede crear, actualizar y eliminar libros, géneros y bibliotecas, y ver todos los préstamos |
| `CLIENT` | Puede crear préstamos, devolver sus propios libros y ver su historial de préstamos |

### Endpoints Públicos (sin autenticación)

- `GET /api/books/**`, `GET /api/genres/**`, `GET /api/libraries/**`
- `POST /api/auth/register`, `POST /api/auth/login`
- Swagger UI (`/swagger-ui/**`, `/v3/api-docs/**`)

---

## 📖 Referencia de la API

Documentación interactiva disponible en: `http://localhost:8080/swagger-ui.html`

### Autenticación `/api/auth`

| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| `POST` | `/api/auth/register` | Registrar un nuevo usuario | Público |
| `POST` | `/api/auth/login` | Iniciar sesión y recibir un token JWT | Público |

#### Ejemplo — Registro

```http
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan@ejemplo.com",
  "password": "secreto123"
}
```

#### Ejemplo — Respuesta de Login

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### Libros `/api/books`

| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| `POST` | `/api/books` | Crear un libro | `ADMIN` |
| `POST` | `/api/books/import` | Importar un libro desde Google Books por ISBN | `ADMIN` |
| `GET` | `/api/books` | Obtener todos los libros (paginado) | Público |
| `GET` | `/api/books/{id}` | Obtener libro por ID | Público |
| `GET` | `/api/books/available` | Obtener libros disponibles (paginado) | Público |
| `GET` | `/api/books/external/{isbn}` | Buscar información de un libro en Google Books por ISBN | Público |
| `PUT` | `/api/books/{id}` | Actualizar un libro | `ADMIN` |
| `DELETE` | `/api/books/{id}` | Eliminar un libro | `ADMIN` |
| `PATCH` | `/api/books/{id}/genres/{genreId}` | Agregar un género a un libro | `ADMIN` |
| `DELETE` | `/api/books/{id}/genres/{genreId}` | Eliminar un género de un libro | `ADMIN` |

#### Ejemplo — Crear Libro

```http
POST /api/books
Authorization: Bearer <token_admin>
Content-Type: application/json

{
  "title": "1984",
  "author": "George Orwell",
  "synopsis": "Una novela de ciencia ficción social distópica.",
  "libraryId": 1,
  "genreIds": [1, 2]
}
```

#### Ejemplo — Importar Libro desde Google Books

```http
POST /api/books/import
Authorization: Bearer <token_admin>
Content-Type: application/json

{
  "isbn": "9780451524935",
  "stock": 5,
  "libraryId": 1,
  "genreIds": [1]
}
```

#### Ejemplo — Respuesta de Libro

```json
{
  "id": 1,
  "title": "1984",
  "author": "George Orwell",
  "synopsis": "Una novela de ciencia ficción social distópica.",
  "libraryName": "Biblioteca Central",
  "genreNames": ["Ficción", "Distopía"]
}
```

---

### Géneros `/api/genres`

| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| `POST` | `/api/genres` | Crear un género | `ADMIN` |
| `GET` | `/api/genres` | Obtener todos los géneros (paginado) | Público |
| `GET` | `/api/genres/{id}` | Obtener género por ID | Público |
| `PUT` | `/api/genres/{id}` | Actualizar un género | `ADMIN` |
| `DELETE` | `/api/genres/{id}` | Eliminar un género | `ADMIN` |

---

### Bibliotecas `/api/libraries`

| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| `POST` | `/api/libraries` | Crear una biblioteca | `ADMIN` |
| `GET` | `/api/libraries` | Obtener todas las bibliotecas (paginado) | Público |
| `GET` | `/api/libraries/{id}` | Obtener biblioteca por ID | Público |
| `PUT` | `/api/libraries/{id}` | Actualizar una biblioteca | `ADMIN` |
| `DELETE` | `/api/libraries/{id}` | Eliminar una biblioteca | `ADMIN` |

---

### Préstamos `/api/loans`

| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| `POST` | `/api/loans` | Crear un préstamo (tomar un libro) | `ADMIN`, `CLIENT` |
| `GET` | `/api/loans/me` | Obtener los préstamos del usuario actual | `ADMIN`, `CLIENT` |
| `GET` | `/api/loans` | Obtener todos los préstamos (paginado) | `ADMIN` |
| `PATCH` | `/api/loans/{id}/return` | Devolver un préstamo | `ADMIN`, `CLIENT` |

#### Ejemplo — Crear Préstamo

```http
POST /api/loans
Authorization: Bearer <token_cliente>
Content-Type: application/json

{
  "bookId": 1
}
```

#### Ejemplo — Respuesta de Préstamo

```json
{
  "id": 1,
  "bookTitle": "1984",
  "userEmail": "juan@ejemplo.com",
  "loanDate": "2025-03-01",
  "dueDate": "2025-03-15",
  "returnDate": null,
  "status": "ACTIVE"
}
```

---

## ⏰ Tareas Programadas

| Tarea | Frecuencia | Descripción |
|---|---|---|
| Verificación de préstamos vencidos | Diariamente a medianoche (`0 0 0 * * *`) | Marca automáticamente como `OVERDUE` los préstamos `ACTIVE` que superaron su fecha de vencimiento |

---

## 👤 Autor

**Matias Roman Diaz**  
GitHub: [matiassddiaz](https://github.com/matiassddiaz)