package com.matias.library.service;

import com.matias.library.dto.BookRequestDTO;
import com.matias.library.dto.BookResponseDTO;
import com.matias.library.dto.ImportBookRequestDTO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService implements IBookService{

    private final BookRepository repository;
    private final LibraryRepository libraryRepository;
    private final GenreRepository genreRepository;
    private final EntityMapper entityMapper;
    private final ExternalBookService externalBookService;

    @Override
    @Transactional
    public BookResponseDTO createBook(BookRequestDTO dto) {

        Library library = libraryRepository.findById(dto.getLibraryId())
                .orElseThrow(() -> new NotFoundException("No library was found using ID: " + dto.getLibraryId()));
        Set<Genre> genres = new HashSet<>(genreRepository.findAllById(dto.getGenreIds()));
        if (genres.size() != dto.getGenreIds().size()) {
            throw new NotFoundException("Some genres were not found");
        }

        Book book = entityMapper.toEntity(dto, library, genres);
        Book savedBook = repository.save(book);
        return entityMapper.toDTO(savedBook);
    }

    @Override
    @Transactional
    public BookResponseDTO importBook(ImportBookRequestDTO dto) {
        if (repository.findByIsbn(dto.getIsbn()).isPresent()) {
            throw new BadRequestException("A book with this ISBN already exists in the inventory.");
        }

        var googleData = externalBookService.fetchBookByIsbn(dto.getIsbn());

        Library library = libraryRepository.findById(dto.getLibraryId())
                .orElseThrow(() -> new NotFoundException("No library was found using ID: " + dto.getLibraryId()));

        Set<Genre> genres = new HashSet<>(genreRepository.findAllById(dto.getGenreIds()));
        if (genres.size() != dto.getGenreIds().size()) {
            throw new NotFoundException("Some genres were not found");
        }

        String authors = googleData.getAuthors() != null ? String.join(", ", googleData.getAuthors()) : "Unknown Author";
        String imageUrl = googleData.getImageLinks() != null ? googleData.getImageLinks().getThumbnail() : null;

        Book book = Book.builder()
                .isbn(dto.getIsbn())
                .title(googleData.getTitle() != null ? googleData.getTitle() : "Unknown Title")
                .author(authors)
                .synopsis(googleData.getDescription())
                .publishedDate(googleData.getPublishedDate())
                .imageUrl(imageUrl)
                .stock(dto.getStock())
                .library(library)
                .genres(genres)
                .build();

        Book savedBook = repository.save(book);
        return entityMapper.toDTO(savedBook);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<BookResponseDTO> getAllBooks(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = repository.findAll(pageable);

        List<BookResponseDTO> listOfBooks = bookPage.getContent().stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());

        return PaginatedResponseDTO.<BookResponseDTO>builder()
                .content(listOfBooks)
                .pageNo(bookPage.getNumber())
                .pageSize(bookPage.getSize())
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .last(bookPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDTO getBookById(Long id) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No book was found using ID:" + id));
        return entityMapper.toDTO(book);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<BookResponseDTO> getAvailableBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Book> bookPage = repository.findByStockGreaterThan(0, pageable);

        List<BookResponseDTO> listOfBooks = bookPage.getContent().stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());

        return PaginatedResponseDTO.<BookResponseDTO>builder()
                .content(listOfBooks)
                .pageNo(bookPage.getNumber())
                .pageSize(bookPage.getSize())
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .last(bookPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public BookResponseDTO updateBook(Long id, BookRequestDTO dto) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No book was found using ID:" + id));

        book.setIsbn(dto.getIsbn());
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setSynopsis(dto.getSynopsis());
        book.setPublishedDate(dto.getPublishedDate());
        book.setImageUrl(dto.getImageUrl());
        book.setStock(dto.getStock());

        Library newLibrary = libraryRepository.findById(dto.getLibraryId())
                .orElseThrow(() -> new NotFoundException("No library was found using ID: " + dto.getLibraryId()));
        book.setLibrary(newLibrary);

        Set<Genre> newGenres = new HashSet<>(genreRepository.findAllById(dto.getGenreIds()));
        if (newGenres.size() != dto.getGenreIds().size())
            throw new NotFoundException("Some genres were not found");
        book.setGenres(newGenres);

        return entityMapper.toDTO(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No book was found using ID:" + id));
        repository.delete(book);
    }

    @Override
    @Transactional
    public BookResponseDTO addGenre(Long id, Long genreId) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No book was found using ID:" + id));
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new NotFoundException("No genre was found using ID:" + genreId));
        book.getGenres().add(genre);
        Book savedBook = repository.save(book);
        return entityMapper.toDTO(savedBook);
    }

    @Override
    @Transactional
    public BookResponseDTO removeGenre(Long id, Long genreId) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No book was found using ID: " + id));

        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new NotFoundException("No genre was found using ID: " + genreId));

        if (!book.getGenres().contains(genre)) {
            throw new BadRequestException("Book did not contain genre: " + genreId);
        }
        book.getGenres().remove(genre);

        return entityMapper.toDTO(book);
    }


}
