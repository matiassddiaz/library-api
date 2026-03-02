package com.matias.library.service;

import com.matias.library.dto.LibraryRequestDTO;
import com.matias.library.dto.LibraryResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.exception.ConflictException;
import com.matias.library.exception.NotFoundException;
import com.matias.library.mapper.EntityMapper;
import com.matias.library.model.Library;
import com.matias.library.repository.LibraryRepository;
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
class LibraryServiceTest {

    @Mock
    private LibraryRepository repository;
    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private LibraryService service;

    // ─── createLibrary ────────────────────────────────────────────────────────

    @Test
    void createLibrary_WhenValid_ShouldReturnDto() {
        LibraryRequestDTO request = new LibraryRequestDTO("Central", "Main St 123");
        Library entity = Library.builder().id(1L).name("Central").address("Main St 123").build();
        LibraryResponseDTO response = LibraryResponseDTO.builder().id(1L).name("Central").address("Main St 123").build();

        when(repository.existsByNameAndAddress("Central", "Main St 123")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(response);

        LibraryResponseDTO result = service.createLibrary(request);

        assertNotNull(result);
        assertEquals("Central", result.getName());
        assertEquals("Main St 123", result.getAddress());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void createLibrary_WhenAlreadyExists_ShouldThrowConflictException() {
        LibraryRequestDTO request = new LibraryRequestDTO("Central", "Main St 123");

        when(repository.existsByNameAndAddress("Central", "Main St 123")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createLibrary(request));
        verify(repository, never()).save(any());
    }

    // ─── getAllLibraries ──────────────────────────────────────────────────────

    @Test
    void getAllLibraries_ShouldReturnPaginatedResponse() {
        Library library = Library.builder().id(1L).name("Central").address("Main St 1").build();
        LibraryResponseDTO dto = LibraryResponseDTO.builder().id(1L).name("Central").address("Main St 1").build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Library> page = new PageImpl<>(List.of(library), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toDTO(library)).thenReturn(dto);

        PaginatedResponseDTO<LibraryResponseDTO> result = service.getAllLibraries(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Central", result.getContent().get(0).getName());
        assertEquals(1, result.getTotalElements());
        assertTrue(result.isLast());
    }

    @Test
    void getAllLibraries_WhenEmpty_ShouldReturnEmptyContent() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Library> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findAll(pageable)).thenReturn(emptyPage);

        PaginatedResponseDTO<LibraryResponseDTO> result = service.getAllLibraries(0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    // ─── getLibraryById ───────────────────────────────────────────────────────

    @Test
    void getLibraryById_WhenExists_ShouldReturnDto() {
        Library entity = Library.builder().id(1L).name("Central").build();
        LibraryResponseDTO response = LibraryResponseDTO.builder().id(1L).name("Central").build();

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(response);

        LibraryResponseDTO result = service.getLibraryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getLibraryById_WhenNotExists_ShouldThrowNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getLibraryById(99L));
    }

    // ─── updateLibrary ────────────────────────────────────────────────────────

    @Test
    void updateLibrary_WhenValid_ShouldReturnUpdatedDto() {
        Library existing = Library.builder().id(1L).name("Old Name").address("Old Address").build();
        LibraryRequestDTO request = new LibraryRequestDTO("New Name", "New Address");
        LibraryResponseDTO response = LibraryResponseDTO.builder().id(1L).name("New Name").address("New Address").build();

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByNameAndAddressAndIdNot("New Name", "New Address", 1L)).thenReturn(false);
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDTO(existing)).thenReturn(response);

        LibraryResponseDTO result = service.updateLibrary(1L, request);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        verify(repository, times(1)).save(existing);
    }

    @Test
    void updateLibrary_WhenNotFound_ShouldThrowNotFoundException() {
        LibraryRequestDTO request = new LibraryRequestDTO("Name", "Address");

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updateLibrary(99L, request));
        verify(repository, never()).save(any());
    }

    @Test
    void updateLibrary_WhenNameAndAddressConflict_ShouldThrowConflictException() {
        Library existing = Library.builder().id(1L).name("Central").address("Old St").build();
        LibraryRequestDTO request = new LibraryRequestDTO("Taken Name", "Taken Address");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByNameAndAddressAndIdNot("Taken Name", "Taken Address", 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.updateLibrary(1L, request));
        verify(repository, never()).save(any());
    }

    // ─── deleteLibrary ────────────────────────────────────────────────────────

    @Test
    void deleteLibrary_WhenExists_ShouldCallDelete() {
        Library library = Library.builder().id(1L).name("Central").build();

        when(repository.findById(1L)).thenReturn(Optional.of(library));

        service.deleteLibrary(1L);

        verify(repository, times(1)).delete(library);
    }

    @Test
    void deleteLibrary_WhenNotExists_ShouldThrowNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deleteLibrary(99L));
        verify(repository, never()).delete(any());
    }
}