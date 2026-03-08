package com.matias.library.service;

import com.matias.library.dto.BookRequestDTO;
import com.matias.library.dto.BookResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;

public interface IBookService {
    BookResponseDTO createBook(BookRequestDTO dto);
    PaginatedResponseDTO<BookResponseDTO> getAllBooks(int page, int size);
    BookResponseDTO getBookById(Long id);
    PaginatedResponseDTO<BookResponseDTO> getAvailableBooks(int page, int size);
    BookResponseDTO updateBook(Long id, BookRequestDTO dto);
    void deleteBook(Long id);
    BookResponseDTO addGenre(Long id, Long genreId);
    BookResponseDTO removeGenre(Long id, Long genreId);
}
