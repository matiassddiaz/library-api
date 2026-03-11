package com.matias.library.controller;


import com.matias.library.dto.BookRequestDTO;
import com.matias.library.dto.BookResponseDTO;
import com.matias.library.dto.ImportBookRequestDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.dto.external.GoogleBooksResponseDTO;
import com.matias.library.service.ExternalBookService;
import com.matias.library.service.IBookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final IBookService bookService;
    private final ExternalBookService externalBookService;

    @PostMapping
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookRequestDTO dto) {
        BookResponseDTO createdBook = bookService.createBook(dto);
        URI location = URI.create("/api/books/" + createdBook.getId());
        return ResponseEntity.created(location).body(createdBook);
    }

    @PostMapping("/import")
    public ResponseEntity<BookResponseDTO> importBook(@Valid @RequestBody ImportBookRequestDTO dto) {
        BookResponseDTO importedBook = bookService.importBook(dto);
        URI location = URI.create("/api/books/" + importedBook.getId());
        return ResponseEntity.created(location).body(importedBook);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<BookResponseDTO>> getAllBooks(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size) {
        return ResponseEntity.ok(bookService.getAllBooks(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Long id) {
        BookResponseDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/available")
    public ResponseEntity<PaginatedResponseDTO<BookResponseDTO>> getAvailableBooks(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size) {
        return ResponseEntity.ok(bookService.getAvailableBooks(page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> updateBook(@PathVariable Long id,
                                                      @Valid @RequestBody BookRequestDTO dto) {
        return ResponseEntity.ok(bookService.updateBook(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/genres/{genreId}")
    public ResponseEntity<BookResponseDTO> addGenre(@PathVariable Long id, @PathVariable Long genreId) {
        return ResponseEntity.ok(bookService.addGenre(id, genreId));
    }

    @DeleteMapping("/{id}/genres/{genreId}")
    public ResponseEntity<BookResponseDTO> removeGenre(@PathVariable Long id, @PathVariable Long genreId) {
        return ResponseEntity.ok(bookService.removeGenre(id, genreId));
    }

    @GetMapping("/external/{isbn}")
    public ResponseEntity<GoogleBooksResponseDTO.VolumeInfo> searchExternalBook(@PathVariable String isbn) {
        return ResponseEntity.ok(externalBookService.fetchBookByIsbn(isbn));
    }
}
