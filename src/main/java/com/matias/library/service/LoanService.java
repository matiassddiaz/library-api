package com.matias.library.service;

import com.matias.library.dto.LoanRequestDTO;
import com.matias.library.dto.LoanResponseDTO;
import com.matias.library.exception.NotFoundException;
import com.matias.library.model.Book;
import com.matias.library.model.Loan;
import com.matias.library.model.User;
import com.matias.library.model.enums.LoanStatus;
import com.matias.library.repository.BookRepository;
import com.matias.library.repository.LoanRepository;
import com.matias.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Transactional
    public LoanResponseDTO createLoan(LoanRequestDTO request) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        String currentUserEmail = authentication.getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new NotFoundException("User not found in database"));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new NotFoundException("Book not found with ID: " + request.getBookId()));

        Loan loan = Loan.builder()
                .user(user)
                .book(book)
                .loanDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(14))
                .status(LoanStatus.ACTIVE)
                .build();

        Loan savedLoan = loanRepository.save(loan);

        return LoanResponseDTO.builder()
                .id(savedLoan.getId())
                .bookTitle(book.getTitle())
                .userEmail(user.getEmail())
                .loanDate(savedLoan.getLoanDate())
                .returnDate(savedLoan.getReturnDate())
                .status(savedLoan.getStatus())
                .build();
    }
}