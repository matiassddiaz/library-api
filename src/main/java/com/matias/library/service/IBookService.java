package com.matias.library.service;

import com.matias.library.dto.BookRequestDTO;
import com.matias.library.dto.BookResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.dto.ImportBookRequestDTO;
import com.matias.library.dto.external.GoogleBooksResponseDTO;

public interface IBookService {
    BookResponseDTO createBook(BookRequestDTO dto);
    BookResponseDTO importBook(ImportBookRequestDTO dto);
    PaginatedResponseDTO<BookResponseDTO> getAllBooks(int page, int size);
    BookResponseDTO getBookById(Long id);
    PaginatedResponseDTO<BookResponseDTO> getAvailableBooks(int page, int size);
    BookResponseDTO updateBook(Long id, BookRequestDTO dto);
    void deleteBook(Long id);
    BookResponseDTO addGenre(Long id, Long genreId);
    BookResponseDTO removeGenre(Long id, Long genreId);
    GoogleBooksResponseDTO.VolumeInfo searchExternalBook(String isbn);
}
