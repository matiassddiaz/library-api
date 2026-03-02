package com.matias.library.service;

import com.matias.library.dto.BookRequestDTO;
import com.matias.library.dto.BookResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.exception.BadRequestException;
import com.matias.library.exception.NotFoundException;
import com.matias.library.mapper.EntityMapper;
import com.matias.library.model.Book;
import com.matias.library.model.Genre;
import com.matias.library.model.Library;
import com.matias.library.repository.BookRepository;
import com.matias.library.repository.GenreRepository;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repository;
    @Mock
    private LibraryRepository libraryRepository;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private BookService service;

    // ─── createBook ───────────────────────────────────────────────────────────

    @Test
    void createBook_WhenValid_ShouldReturnDto() {
        Library library = Library.builder().id(1L).name("Central").build();
        Genre genre = new Genre(1L, "Fiction");
        BookRequestDTO request = BookRequestDTO.builder()
                .title("1984").author("Orwell").libraryId(1L).genreIds(Set.of(1L)).build();
        Book entity = Book.builder().id(1L).title("1984").build();
        BookResponseDTO response = BookResponseDTO.builder().id(1L).title("1984").build();

        when(libraryRepository.findById(1L)).thenReturn(Optional.of(library));
        when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(genre));
        when(mapper.toEntity(request, library, Set.of(genre))).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(response);

        BookResponseDTO result = service.createBook(request);

        assertNotNull(result);
        assertEquals("1984", result.getTitle());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void createBook_WhenLibraryNotFound_ShouldThrowNotFoundException() {
        BookRequestDTO request = BookRequestDTO.builder()
                .title("1984").author("Orwell").libraryId(99L).genreIds(Set.of(1L)).build();

        when(libraryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.createBook(request));
        verify(repository, never()).save(any());
    }

    @Test
    void createBook_WhenSomeGenresNotFound_ShouldThrowNotFoundException() {
        Library library = Library.builder().id(1L).name("Central").build();
        BookRequestDTO request = BookRequestDTO.builder()
                .title("1984").author("Orwell").libraryId(1L).genreIds(Set.of(1L, 2L)).build();

        when(libraryRepository.findById(1L)).thenReturn(Optional.of(library));
        // returns only 1 genre but 2 were requested
        when(genreRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(new Genre(1L, "Fiction")));

        assertThrows(NotFoundException.class, () -> service.createBook(request));
        verify(repository, never()).save(any());
    }

    // ─── getAllBooks ──────────────────────────────────────────────────────────

    @Test
    void getAllBooks_ShouldReturnPaginatedResponse() {
        Book book = Book.builder().id(1L).title("1984").build();
        BookResponseDTO dto = BookResponseDTO.builder().id(1L).title("1984").build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(book), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toDTO(book)).thenReturn(dto);

        PaginatedResponseDTO<BookResponseDTO> result = service.getAllBooks(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPageNo());
        assertTrue(result.isLast());
    }

    @Test
    void getAllBooks_WhenEmpty_ShouldReturnEmptyContent() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findAll(pageable)).thenReturn(emptyPage);

        PaginatedResponseDTO<BookResponseDTO> result = service.getAllBooks(0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    // ─── getBookById ──────────────────────────────────────────────────────────

    @Test
    void getBookById_WhenExists_ShouldReturnDto() {
        Book book = Book.builder().id(1L).title("1984").build();
        BookResponseDTO response = BookResponseDTO.builder().id(1L).title("1984").build();

        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(mapper.toDTO(book)).thenReturn(response);

        BookResponseDTO result = service.getBookById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getBookById_WhenNotExists_ShouldThrowNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getBookById(99L));
    }

    // ─── getAvailableBooks ────────────────────────────────────────────────────

    @Test
    void getAvailableBooks_ShouldReturnOnlyAvailableBooks() {
        Book availableBook = Book.builder().id(1L).title("1984").rented(false).build();
        BookResponseDTO dto = BookResponseDTO.builder().id(1L).title("1984").rented(false).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(availableBook), pageable, 1);

        when(repository.findByRented(false, pageable)).thenReturn(page);
        when(mapper.toDTO(availableBook)).thenReturn(dto);

        PaginatedResponseDTO<BookResponseDTO> result = service.getAvailableBooks(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertFalse(result.getContent().get(0).isRented());
    }

    // ─── updateBook ───────────────────────────────────────────────────────────

    @Test
    void updateBook_WhenValid_ShouldReturnUpdatedDto() {
        Book existing = Book.builder().id(1L).title("Old Title").build();
        Library library = Library.builder().id(1L).name("Central").build();
        Genre genre = new Genre(1L, "Fiction");
        BookRequestDTO request = BookRequestDTO.builder()
                .title("New Title").author("Orwell").libraryId(1L).genreIds(Set.of(1L)).build();
        BookResponseDTO response = BookResponseDTO.builder().id(1L).title("New Title").build();

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(libraryRepository.findById(1L)).thenReturn(Optional.of(library));
        when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(genre));
        when(mapper.toDTO(existing)).thenReturn(response);

        BookResponseDTO result = service.updateBook(1L, request);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
    }

    @Test
    void updateBook_WhenBookNotFound_ShouldThrowNotFoundException() {
        BookRequestDTO request = BookRequestDTO.builder()
                .title("X").author("Y").libraryId(1L).genreIds(Set.of(1L)).build();

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updateBook(99L, request));
        verify(repository, never()).save(any());
    }

    @Test
    void updateBook_WhenLibraryNotFound_ShouldThrowNotFoundException() {
        Book existing = Book.builder().id(1L).title("1984").build();
        BookRequestDTO request = BookRequestDTO.builder()
                .title("1984").author("Orwell").libraryId(99L).genreIds(Set.of(1L)).build();

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(libraryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updateBook(1L, request));
        verify(repository, never()).save(any());
    }

    @Test
    void updateBook_WhenSomeGenresNotFound_ShouldThrowNotFoundException() {
        Book existing = Book.builder().id(1L).title("1984").build();
        Library library = Library.builder().id(1L).build();
        BookRequestDTO request = BookRequestDTO.builder()
                .title("1984").author("Orwell").libraryId(1L).genreIds(Set.of(1L, 2L)).build();

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(libraryRepository.findById(1L)).thenReturn(Optional.of(library));
        when(genreRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(new Genre(1L, "Fiction")));

        assertThrows(NotFoundException.class, () -> service.updateBook(1L, request));
        verify(repository, never()).save(any());
    }

    // ─── deleteBook ───────────────────────────────────────────────────────────

    @Test
    void deleteBook_WhenExists_ShouldCallDelete() {
        Book book = Book.builder().id(1L).title("1984").build();

        when(repository.findById(1L)).thenReturn(Optional.of(book));

        service.deleteBook(1L);

        verify(repository, times(1)).delete(book);
    }

    @Test
    void deleteBook_WhenNotExists_ShouldThrowNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deleteBook(99L));
        verify(repository, never()).delete(any());
    }

    // ─── rentBook ─────────────────────────────────────────────────────────────

    @Test
    void rentBook_WhenBookIsAvailable_ShouldReturnRentedBook() {
        Book availableBook = Book.builder().id(1L).title("1984").rented(false).build();
        BookResponseDTO response = BookResponseDTO.builder().id(1L).title("1984").rented(true).build();

        when(repository.findById(1L)).thenReturn(Optional.of(availableBook));
        when(mapper.toDTO(availableBook)).thenReturn(response);

        BookResponseDTO result = service.rentBook(1L);

        assertNotNull(result);
        assertTrue(result.isRented());
    }

    @Test
    void rentBook_WhenBookIsAlreadyRented_ShouldThrowBadRequestException() {
        Book rentedBook = Book.builder().id(1L).title("1984").rented(true).build();

        when(repository.findById(1L)).thenReturn(Optional.of(rentedBook));

        assertThrows(BadRequestException.class, () -> service.rentBook(1L));
        verify(repository, never()).save(any());
    }

    @Test
    void rentBook_WhenBookDoesNotExist_ShouldThrowNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.rentBook(99L));
    }

    // ─── returnBook ───────────────────────────────────────────────────────────

    @Test
    void returnBook_WhenBookIsRented_ShouldReturnAvailableBook() {
        Book rentedBook = Book.builder().id(1L).title("1984").rented(true).build();
        BookResponseDTO response = BookResponseDTO.builder().id(1L).title("1984").rented(false).build();

        when(repository.findById(1L)).thenReturn(Optional.of(rentedBook));
        when(mapper.toDTO(rentedBook)).thenReturn(response);

        BookResponseDTO result = service.returnBook(1L);

        assertNotNull(result);
        assertFalse(result.isRented());
    }

    @Test
    void returnBook_WhenBookIsNotRented_ShouldThrowBadRequestException() {
        Book availableBook = Book.builder().id(1L).title("1984").rented(false).build();

        when(repository.findById(1L)).thenReturn(Optional.of(availableBook));

        assertThrows(BadRequestException.class, () -> service.returnBook(1L));
        verify(repository, never()).save(any());
    }

    @Test
    void returnBook_WhenBookDoesNotExist_ShouldThrowNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.returnBook(99L));
    }

    // ─── addGenre ─────────────────────────────────────────────────────────────

    @Test
    void addGenre_WhenBookAndGenreExist_ShouldReturnDto() {
        Book book = Book.builder().id(1L).title("1984").genres(new java.util.HashSet<>()).build();
        Genre genre = new Genre(2L, "Sci-Fi");
        BookResponseDTO response = BookResponseDTO.builder().id(1L).title("1984").build();

        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(genreRepository.findById(2L)).thenReturn(Optional.of(genre));
        when(repository.save(any(Book.class))).thenReturn(book);
        when(mapper.toDTO(book)).thenReturn(response);

        BookResponseDTO result = service.addGenre(1L, 2L);

        assertNotNull(result);
        assertTrue(book.getGenres().contains(genre));
        verify(repository, times(1)).save(book);
    }

    @Test
    void addGenre_WhenBookNotFound_ShouldThrowNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.addGenre(99L, 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void addGenre_WhenGenreNotFound_ShouldThrowNotFoundException() {
        Book book = Book.builder().id(1L).title("1984").build();

        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(genreRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.addGenre(1L, 99L));
        verify(repository, never()).save(any());
    }


    // ─── removeGenre ─────────────────────────────────────────────────────────────


    @Test
    void removeGenre_WhenValid_ShouldReturnUpdatedBook() {
        Genre genre = new Genre();
        genre.setId(2L);
        genre.setName("Sci-Fi");

        Book book = new Book();
        book.setId(1L);
        book.setGenres(new java.util.HashSet<>(java.util.List.of(genre)));

        BookResponseDTO expectedResponse = BookResponseDTO.builder().id(1L).build();

        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(genreRepository.findById(2L)).thenReturn(Optional.of(genre));
        when(mapper.toDTO(book)).thenReturn(expectedResponse);

        BookResponseDTO result = service.removeGenre(1L, 2L);

        assertNotNull(result);
        assertFalse(book.getGenres().contains(genre));
    }

    @Test
    void removeGenre_WhenBookNotContainsGenre_ShouldThrowBadRequestException() {
        Genre genre = new Genre();
        genre.setId(2L);

        Book book = new Book();
        book.setId(1L);
        book.setGenres(new java.util.HashSet<>());

        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(genreRepository.findById(2L)).thenReturn(Optional.of(genre));

        assertThrows(BadRequestException.class, () -> service.removeGenre(1L, 2L));
        verify(repository, never()).save(any());
    }
}