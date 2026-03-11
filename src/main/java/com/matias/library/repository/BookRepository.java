package com.matias.library.repository;

import com.matias.library.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByStockGreaterThan(int stock, Pageable pageable);
    Optional<Book> findByIsbn(String isbn);
}


