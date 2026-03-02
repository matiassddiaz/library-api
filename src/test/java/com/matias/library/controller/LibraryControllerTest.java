package com.matias.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matias.library.dto.LibraryRequestDTO;
import com.matias.library.dto.LibraryResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.exception.ConflictException;
import com.matias.library.exception.NotFoundException;
import com.matias.library.service.LibraryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LibraryController.class)
class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibraryService libraryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─── POST /api/libraries ──────────────────────────────────────────────────

    @Test
    void createLibrary_WhenValid_ShouldReturn201WithLocationHeader() throws Exception {
        LibraryRequestDTO request = new LibraryRequestDTO("Central", "Main St 123");
        LibraryResponseDTO response = LibraryResponseDTO.builder().id(1L).name("Central").address("Main St 123").build();

        when(libraryService.createLibrary(any(LibraryRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/libraries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/libraries/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Central"))
                .andExpect(jsonPath("$.address").value("Main St 123"));
    }

    @Test
    void createLibrary_WhenNameIsBlank_ShouldReturn400() throws Exception {
        LibraryRequestDTO request = new LibraryRequestDTO("", "Main St 123");

        mockMvc.perform(post("/api/libraries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void createLibrary_WhenAddressIsBlank_ShouldReturn400() throws Exception {
        LibraryRequestDTO request = new LibraryRequestDTO("Central", "");

        mockMvc.perform(post("/api/libraries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.address").exists());
    }

    @Test
    void createLibrary_WhenAlreadyExists_ShouldReturn409() throws Exception {
        LibraryRequestDTO request = new LibraryRequestDTO("Central", "Main St 123");

        when(libraryService.createLibrary(any(LibraryRequestDTO.class)))
                .thenThrow(new ConflictException("A library already exists with that name and address"));

        mockMvc.perform(post("/api/libraries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ─── GET /api/libraries ───────────────────────────────────────────────────

    @Test
    void getAllLibraries_ShouldReturn200WithPaginatedResponse() throws Exception {
        LibraryResponseDTO dto = LibraryResponseDTO.builder().id(1L).name("Central").address("Main St 1").build();
        PaginatedResponseDTO<LibraryResponseDTO> paginated = PaginatedResponseDTO.<LibraryResponseDTO>builder()
                .content(List.of(dto)).pageNo(0).pageSize(10).totalElements(1).totalPages(1).last(true).build();

        when(libraryService.getAllLibraries(0, 10)).thenReturn(paginated);

        mockMvc.perform(get("/api/libraries").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Central"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    // ─── GET /api/libraries/{id} ──────────────────────────────────────────────

    @Test
    void getLibraryById_WhenExists_ShouldReturn200() throws Exception {
        LibraryResponseDTO response = LibraryResponseDTO.builder().id(1L).name("Central").address("Main St 123").build();

        when(libraryService.getLibraryById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/libraries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Central"))
                .andExpect(jsonPath("$.address").value("Main St 123"));
    }

    @Test
    void getLibraryById_WhenNotExists_ShouldReturn404() throws Exception {
        when(libraryService.getLibraryById(99L)).thenThrow(new NotFoundException("No library was found using ID: 99"));

        mockMvc.perform(get("/api/libraries/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─── PUT /api/libraries/{id} ──────────────────────────────────────────────

    @Test
    void updateLibrary_WhenValid_ShouldReturn200() throws Exception {
        LibraryRequestDTO request = new LibraryRequestDTO("New Name", "New Address");
        LibraryResponseDTO response = LibraryResponseDTO.builder().id(1L).name("New Name").address("New Address").build();

        when(libraryService.updateLibrary(eq(1L), any(LibraryRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/libraries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.address").value("New Address"));
    }

    @Test
    void updateLibrary_WhenNotFound_ShouldReturn404() throws Exception {
        LibraryRequestDTO request = new LibraryRequestDTO("Name", "Address");

        when(libraryService.updateLibrary(eq(99L), any(LibraryRequestDTO.class)))
                .thenThrow(new NotFoundException("No library was found using ID: 99"));

        mockMvc.perform(put("/api/libraries/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateLibrary_WhenConflict_ShouldReturn409() throws Exception {
        LibraryRequestDTO request = new LibraryRequestDTO("Taken Name", "Taken Address");

        when(libraryService.updateLibrary(eq(1L), any(LibraryRequestDTO.class)))
                .thenThrow(new ConflictException("A library already exists with that name and address"));

        mockMvc.perform(put("/api/libraries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void updateLibrary_WhenNameIsBlank_ShouldReturn400() throws Exception {
        LibraryRequestDTO request = new LibraryRequestDTO("", "Address");

        mockMvc.perform(put("/api/libraries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    // ─── DELETE /api/libraries/{id} ───────────────────────────────────────────

    @Test
    void deleteLibrary_WhenExists_ShouldReturn204() throws Exception {
        doNothing().when(libraryService).deleteLibrary(1L);

        mockMvc.perform(delete("/api/libraries/1"))
                .andExpect(status().isNoContent());

        verify(libraryService, times(1)).deleteLibrary(1L);
    }

    @Test
    void deleteLibrary_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new NotFoundException("No library was found using ID: 99")).when(libraryService).deleteLibrary(99L);

        mockMvc.perform(delete("/api/libraries/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}