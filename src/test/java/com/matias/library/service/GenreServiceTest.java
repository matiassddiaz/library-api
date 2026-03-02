package com.matias.library.service;

import com.matias.library.dto.GenreRequestDTO;
import com.matias.library.dto.GenreResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.exception.ConflictException;
import com.matias.library.exception.NotFoundException;
import com.matias.library.mapper.EntityMapper;
import com.matias.library.model.Genre;
import com.matias.library.repository.GenreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreRepository repository;
    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private GenreService service;

    // ─── createGenre ──────────────────────────────────────────────────────────

    @Test
    void createGenre_WhenNameDoesNotExist_ShouldReturnDto() {
        GenreRequestDTO request = new GenreRequestDTO("Fantasy");
        Genre entity = new Genre(1L, "Fantasy");
        GenreResponseDTO response = new GenreResponseDTO(1L, "Fantasy");

        when(repository.existsByName("Fantasy")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(response);

        GenreResponseDTO result = service.createGenre(request);

        assertNotNull(result);
        assertEquals("Fantasy", result.getName());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void createGenre_WhenNameAlreadyExists_ShouldThrowConflictException() {
        GenreRequestDTO request = new GenreRequestDTO("Horror");

        when(repository.existsByName("Horror")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createGenre(request));
        verify(repository, never()).save(any());
    }

    // ─── getAllGenres ─────────────────────────────────────────────────────────

    @Test
    void getAllGenres_ShouldReturnPaginatedResponse() {
        Genre genre = new Genre(1L, "Sci-Fi");
        GenreResponseDTO dto = new GenreResponseDTO(1L, "Sci-Fi");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Genre> page = new PageImpl<>(List.of(genre), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toDTO(genre)).thenReturn(dto);

        PaginatedResponseDTO<GenreResponseDTO> result = service.getAllGenres(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Sci-Fi", result.getContent().get(0).getName());
        assertEquals(1, result.getTotalElements());
        assertTrue(result.isLast());
    }

    @Test
    void getAllGenres_WhenEmpty_ShouldReturnEmptyContent() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Genre> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findAll(pageable)).thenReturn(emptyPage);

        PaginatedResponseDTO<GenreResponseDTO> result = service.getAllGenres(0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    // ─── getGenreById ─────────────────────────────────────────────────────────

    @Test
    void getGenreById_WhenExists_ShouldReturnDto() {
        Genre genre = new Genre(1L, "Horror");
        GenreResponseDTO response = new GenreResponseDTO(1L, "Horror");

        when(repository.findById(1L)).thenReturn(Optional.of(genre));
        when(mapper.toDTO(genre)).thenReturn(response);

        GenreResponseDTO result = service.getGenreById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Horror", result.getName());
    }

    @Test
    void getGenreById_WhenNotExists_ShouldThrowNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getGenreById(99L));
    }

    // ─── updateGenre ──────────────────────────────────────────────────────────

    @Test
    void updateGenre_WhenValid_ShouldReturnUpdatedDto() {
        Genre existing = new Genre(1L, "Old Name");
        GenreRequestDTO request = new GenreRequestDTO("New Name");
        GenreResponseDTO response = new GenreResponseDTO(1L, "New Name");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByNameAndIdNot("New Name", 1L)).thenReturn(false);
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDTO(existing)).thenReturn(response);

        GenreResponseDTO result = service.updateGenre(1L, request);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        verify(repository, times(1)).save(existing);
    }

    @Test
    void updateGenre_WhenGenreNotFound_ShouldThrowNotFoundException() {
        GenreRequestDTO request = new GenreRequestDTO("Name");

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updateGenre(99L, request));
        verify(repository, never()).save(any());
    }

    @Test
    void updateGenre_WhenNameConflictsWithAnotherGenre_ShouldThrowConflictException() {
        Genre existing = new Genre(1L, "Fiction");
        GenreRequestDTO request = new GenreRequestDTO("Horror");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByNameAndIdNot("Horror", 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.updateGenre(1L, request));
        verify(repository, never()).save(any());
    }

    // ─── deleteGenre ──────────────────────────────────────────────────────────

    @Test
    void deleteGenre_WhenExists_ShouldCallDelete() {
        Genre genre = new Genre(1L, "Fantasy");

        when(repository.findById(1L)).thenReturn(Optional.of(genre));

        service.deleteGenre(1L);

        verify(repository, times(1)).delete(genre);
    }

    @Test
    void deleteGenre_WhenNotExists_ShouldThrowNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deleteGenre(99L));
        verify(repository, never()).delete(any());
    }
}