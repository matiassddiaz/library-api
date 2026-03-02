package com.matias.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matias.library.dto.BookRequestDTO;
import com.matias.library.dto.BookResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.exception.BadRequestException;
import com.matias.library.exception.NotFoundException;
import com.matias.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─── POST /api/books ──────────────────────────────────────────────────────

    @Test
    void createBook_WhenValid_ShouldReturn201WithLocationHeader() throws Exception {
        BookRequestDTO request = BookRequestDTO.builder()
                .title("1984").author("George Orwell").libraryId(1L).genreIds(Set.of(1L)).build();
        BookResponseDTO response = BookResponseDTO.builder().id(1L).title("1984").rented(false).build();

        when(bookService.createBook(any(BookRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/books/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("1984"));
    }

    @Test
    void createBook_WhenTitleIsBlank_ShouldReturn400() throws Exception {
        BookRequestDTO request = BookRequestDTO.builder()
                .title("").author("Orwell").libraryId(1L).genreIds(Set.of(1L)).build();

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void createBook_WhenAuthorIsBlank_ShouldReturn400() throws Exception {
        BookRequestDTO request = BookRequestDTO.builder()
                .title("1984").author("").libraryId(1L).genreIds(Set.of(1L)).build();

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.author").exists());
    }

    @Test
    void createBook_WhenLibraryIdIsNull_ShouldReturn400() throws Exception {
        BookRequestDTO request = BookRequestDTO.builder()
                .title("1984").author("Orwell").libraryId(null).genreIds(Set.of(1L)).build();

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.libraryId").exists());
    }

    @Test
    void createBook_WhenGenreIdsIsEmpty_ShouldReturn400() throws Exception {
        BookRequestDTO request = BookRequestDTO.builder()
                .title("1984").author("Orwell").libraryId(1L).genreIds(Set.of()).build();

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.genreIds").exists());
    }

    // ─── GET /api/books ───────────────────────────────────────────────────────

    @Test
    void getAllBooks_ShouldReturn200WithPaginatedResponse() throws Exception {
        BookResponseDTO dto = BookResponseDTO.builder().id(1L).title("1984").build();
        PaginatedResponseDTO<BookResponseDTO> paginated = PaginatedResponseDTO.<BookResponseDTO>builder()
                .content(List.of(dto)).pageNo(0).pageSize(10).totalElements(1).totalPages(1).last(true).build();

        when(bookService.getAllBooks(0, 10)).thenReturn(paginated);

        mockMvc.perform(get("/api/books").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    // ─── GET /api/books/{id} ──────────────────────────────────────────────────

    @Test
    void getBookById_WhenExists_ShouldReturn200() throws Exception {
        BookResponseDTO response = BookResponseDTO.builder().id(1L).title("1984").rented(false).build();

        when(bookService.getBookById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("1984"))
                .andExpect(jsonPath("$.rented").value(false));
    }

    @Test
    void getBookById_WhenNotExists_ShouldReturn404() throws Exception {
        when(bookService.getBookById(99L)).thenThrow(new NotFoundException("No book was found using ID: 99"));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─── GET /api/books/available ─────────────────────────────────────────────

    @Test
    void getAvailableBooks_ShouldReturn200WithAvailableBooks() throws Exception {
        BookResponseDTO dto = BookResponseDTO.builder().id(1L).title("1984").rented(false).build();
        PaginatedResponseDTO<BookResponseDTO> paginated = PaginatedResponseDTO.<BookResponseDTO>builder()
                .content(List.of(dto)).pageNo(0).pageSize(10).totalElements(1).totalPages(1).last(true).build();

        when(bookService.getAvailableBooks(0, 10)).thenReturn(paginated);

        mockMvc.perform(get("/api/books/available").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rented").value(false))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ─── PUT /api/books/{id} ──────────────────────────────────────────────────

    @Test
    void updateBook_WhenValid_ShouldReturn200() throws Exception {
        BookRequestDTO request = BookRequestDTO.builder()
                .title("Updated Title").author("Orwell").libraryId(1L).genreIds(Set.of(1L)).build();
        BookResponseDTO response = BookResponseDTO.builder().id(1L).title("Updated Title").build();

        when(bookService.updateBook(eq(1L), any(BookRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updateBook_WhenNotFound_ShouldReturn404() throws Exception {
        BookRequestDTO request = BookRequestDTO.builder()
                .title("Title").author("Author").libraryId(1L).genreIds(Set.of(1L)).build();

        when(bookService.updateBook(eq(99L), any(BookRequestDTO.class)))
                .thenThrow(new NotFoundException("No book was found using ID: 99"));

        mockMvc.perform(put("/api/books/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─── DELETE /api/books/{id} ───────────────────────────────────────────────

    @Test
    void deleteBook_WhenExists_ShouldReturn204() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBook(1L);
    }

    @Test
    void deleteBook_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new NotFoundException("No book was found using ID: 99")).when(bookService).deleteBook(99L);

        mockMvc.perform(delete("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─── PATCH /api/books/{id}/rent ───────────────────────────────────────────

    @Test
    void rentBook_WhenAvailable_ShouldReturn200WithRentedTrue() throws Exception {
        BookResponseDTO response = BookResponseDTO.builder().id(1L).title("1984").rented(true).build();

        when(bookService.rentBook(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/books/1/rent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rented").value(true));
    }

    @Test
    void rentBook_WhenAlreadyRented_ShouldReturn400() throws Exception {
        when(bookService.rentBook(1L)).thenThrow(new BadRequestException("Book is already rented."));

        mockMvc.perform(patch("/api/books/1/rent"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void rentBook_WhenNotFound_ShouldReturn404() throws Exception {
        when(bookService.rentBook(99L)).thenThrow(new NotFoundException("No book was found using ID: 99"));

        mockMvc.perform(patch("/api/books/99/rent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─── PATCH /api/books/{id}/return ─────────────────────────────────────────

    @Test
    void returnBook_WhenRented_ShouldReturn200WithRentedFalse() throws Exception {
        BookResponseDTO response = BookResponseDTO.builder().id(1L).title("1984").rented(false).build();

        when(bookService.returnBook(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/books/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rented").value(false));
    }

    @Test
    void returnBook_WhenNotRented_ShouldReturn400() throws Exception {
        when(bookService.returnBook(1L)).thenThrow(new BadRequestException("Book was not rented"));

        mockMvc.perform(patch("/api/books/1/return"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─── PATCH /api/books/{id}/genres/{genreId} ───────────────────────────────

    @Test
    void addGenre_WhenValid_ShouldReturn200() throws Exception {
        BookResponseDTO response = BookResponseDTO.builder().id(1L).title("1984").build();

        when(bookService.addGenre(1L, 2L)).thenReturn(response);

        mockMvc.perform(patch("/api/books/1/genres/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void addGenre_WhenGenreNotFound_ShouldReturn404() throws Exception {
        when(bookService.addGenre(1L, 99L)).thenThrow(new NotFoundException("No genre was found using ID: 99"));

        mockMvc.perform(patch("/api/books/1/genres/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }


    // ─── DELETE /api/books/{id}/genres/{genreId} ───────────────────────────────

    @Test
    void removeGenre_WhenValid_ShouldReturn200() throws Exception {
        BookResponseDTO response = BookResponseDTO.builder().id(1L).title("1984").build();

        when(bookService.removeGenre(1L, 2L)).thenReturn(response);

        mockMvc.perform(delete("/api/books/1/genres/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}