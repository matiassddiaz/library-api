package com.matias.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matias.library.dto.GenreRequestDTO;
import com.matias.library.dto.GenreResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.exception.ConflictException;
import com.matias.library.exception.NotFoundException;
import com.matias.library.security.JwtService;
import com.matias.library.service.GenreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GenreController.class)
@AutoConfigureMockMvc(addFilters = false)
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─── POST /api/genres ─────────────────────────────────────────────────────

    @Test
    void createGenre_WhenValid_ShouldReturn201WithLocationHeader() throws Exception {
        GenreRequestDTO request = new GenreRequestDTO("Horror");
        GenreResponseDTO response = new GenreResponseDTO(1L, "Horror");

        when(genreService.createGenre(any(GenreRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/genres/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Horror"));
    }

    @Test
    void createGenre_WhenNameIsBlank_ShouldReturn400() throws Exception {
        GenreRequestDTO request = new GenreRequestDTO("");

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void createGenre_WhenNameAlreadyExists_ShouldReturn409() throws Exception {
        GenreRequestDTO request = new GenreRequestDTO("Fantasy");

        when(genreService.createGenre(any(GenreRequestDTO.class)))
                .thenThrow(new ConflictException("Genre name already exists: Fantasy"));

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ─── GET /api/genres ──────────────────────────────────────────────────────

    @Test
    void getAllGenres_ShouldReturn200WithPaginatedResponse() throws Exception {
        GenreResponseDTO dto = new GenreResponseDTO(1L, "Sci-Fi");
        PaginatedResponseDTO<GenreResponseDTO> paginated = PaginatedResponseDTO.<GenreResponseDTO>builder()
                .content(List.of(dto)).pageNo(0).pageSize(10).totalElements(1).totalPages(1).last(true).build();

        when(genreService.getAllGenres(0, 10)).thenReturn(paginated);

        mockMvc.perform(get("/api/genres").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Sci-Fi"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    // ─── GET /api/genres/{id} ─────────────────────────────────────────────────

    @Test
    void getGenreById_WhenExists_ShouldReturn200() throws Exception {
        GenreResponseDTO response = new GenreResponseDTO(1L, "Sci-Fi");

        when(genreService.getGenreById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/genres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sci-Fi"));
    }

    @Test
    void getGenreById_WhenNotExists_ShouldReturn404() throws Exception {
        when(genreService.getGenreById(99L)).thenThrow(new NotFoundException("No genre was found using ID: 99"));

        mockMvc.perform(get("/api/genres/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─── PUT /api/genres/{id} ─────────────────────────────────────────────────

    @Test
    void updateGenre_WhenValid_ShouldReturn200() throws Exception {
        GenreRequestDTO request = new GenreRequestDTO("Updated");
        GenreResponseDTO response = new GenreResponseDTO(1L, "Updated");

        when(genreService.updateGenre(eq(1L), any(GenreRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/genres/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void updateGenre_WhenNotFound_ShouldReturn404() throws Exception {
        GenreRequestDTO request = new GenreRequestDTO("Name");

        when(genreService.updateGenre(eq(99L), any(GenreRequestDTO.class)))
                .thenThrow(new NotFoundException("No genre was found using ID: 99"));

        mockMvc.perform(put("/api/genres/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateGenre_WhenNameConflicts_ShouldReturn409() throws Exception {
        GenreRequestDTO request = new GenreRequestDTO("Taken");

        when(genreService.updateGenre(eq(1L), any(GenreRequestDTO.class)))
                .thenThrow(new ConflictException("Genre name already exists: Taken"));

        mockMvc.perform(put("/api/genres/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void updateGenre_WhenNameIsBlank_ShouldReturn400() throws Exception {
        GenreRequestDTO request = new GenreRequestDTO("");

        mockMvc.perform(put("/api/genres/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    // ─── DELETE /api/genres/{id} ──────────────────────────────────────────────

    @Test
    void deleteGenre_WhenExists_ShouldReturn204() throws Exception {
        doNothing().when(genreService).deleteGenre(1L);

        mockMvc.perform(delete("/api/genres/1"))
                .andExpect(status().isNoContent());

        verify(genreService, times(1)).deleteGenre(1L);
    }

    @Test
    void deleteGenre_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new NotFoundException("No genre was found using ID: 99")).when(genreService).deleteGenre(99L);

        mockMvc.perform(delete("/api/genres/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}