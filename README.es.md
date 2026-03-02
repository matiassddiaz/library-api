> 🇺🇸 README also available in [English](README.md)

# 📚 API de Gestión de Biblioteca

Una API RESTful para gestionar un sistema de biblioteca construida con **Spring Boot**. Soporta operaciones CRUD completas para libros, géneros y bibliotecas, incluyendo gestión de alquiler de libros.  
Construida utilizando una arquitectura clásica por capas (Controller → Service → Repository → Database), con transferencia de datos basada en DTOs, manejo centralizado de excepciones y endpoints paginados para garantizar una clara separación de responsabilidades, escalabilidad y capacidad de prueba. Completamente testeada con JUnit y Mockito.

---

## 🧰 Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4 |
| ORM | Spring Data JPA / Hibernate |
| Base de Datos | MySQL 8 |
| Validación | Spring Validation (Jakarta) |
| Documentación | SpringDoc OpenAPI (Swagger UI) |
| Contenedores | Docker / Docker Compose |
| Herramienta de Build | Maven |
| Utilidades | Lombok |

---

## 📦 Modelo de Dominio

```
Library ──< Book >── Genre
```

- Una **Biblioteca** tiene muchos **Libros**
- Un **Libro** pertenece a una **Biblioteca** y tiene muchos **Géneros**
- Un **Libro** puede ser alquilado o devuelto

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

## 📖 Referencia de la API

Documentación interactiva disponible en: `http://localhost:8080/swagger-ui.html`

### Libros `/api/books`

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/books` | Crear un libro |
| `GET` | `/api/books` | Obtener todos los libros (paginado) |
| `GET` | `/api/books/{id}` | Obtener libro por ID |
| `GET` | `/api/books/available` | Obtener libros disponibles (no alquilados) (paginado) |
| `PUT` | `/api/books/{id}` | Actualizar un libro |
| `DELETE` | `/api/books/{id}` | Eliminar un libro |
| `PATCH` | `/api/books/{id}/rent` | Alquilar un libro |
| `PATCH` | `/api/books/{id}/return` | Devolver un libro |
| `PATCH` | `/api/books/{id}/genres/{genreId}` | Agregar un género a un libro |
| `DELETE` | `/api/books/{id}/genres/{genreId}` | Eliminar un género de un libro |

### Géneros `/api/genres`

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/genres` | Crear un género |
| `GET` | `/api/genres` | Obtener todos los géneros |
| `GET` | `/api/genres/{id}` | Obtener género por ID |
| `PUT` | `/api/genres/{id}` | Actualizar un género |
| `DELETE` | `/api/genres/{id}` | Eliminar un género |

### Bibliotecas `/api/libraries`

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/libraries` | Crear una biblioteca |
| `GET` | `/api/libraries` | Obtener todas las bibliotecas |
| `GET` | `/api/libraries/{id}` | Obtener biblioteca por ID |
| `PUT` | `/api/libraries/{id}` | Actualizar una biblioteca |
| `DELETE` | `/api/libraries/{id}` | Eliminar una biblioteca |

---

### Ejemplo de Solicitud — Crear Libro

```http
POST /api/books
Content-Type: application/json

{
  "title": "1984",
  "author": "George Orwell",
  "synopsis": "Una novela de ciencia ficción social distópica.",
  "libraryId": 1,
  "genreIds": [1, 2]
}
```

### Ejemplo de Respuesta

```json
{
  "id": 1,
  "title": "1984",
  "author": "George Orwell",
  "synopsis": "Una novela de ciencia ficción social distópica.",
  "rented": false,
  "libraryName": "Biblioteca Central",
  "genreNames": ["Ficción", "Distopía"]
}
```

---

## 👤 Autor

**Matias Roman Diaz**  
GitHub: [matiassddiaz](https://github.com/matiassddiaz)
