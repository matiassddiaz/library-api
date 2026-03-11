package com.matias.library.service;

import com.matias.library.dto.LoanRequestDTO;
import com.matias.library.dto.LoanResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.exception.BadRequestException;
import com.matias.library.exception.ForbiddenException;
import com.matias.library.exception.NotFoundException;
import com.matias.library.model.Book;
import com.matias.library.model.Loan;
import com.matias.library.model.enums.LoanStatus;
import com.matias.library.model.User;
import com.matias.library.repository.BookRepository;
import com.matias.library.repository.LoanRepository;
import com.matias.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService implements ILoanService {
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LoanResponseDTO createLoan(LoanRequestDTO request) {

        User user = getCurrentUser();
        String currentUserEmail = user.getEmail();

        boolean hasOverdueBooks = loanRepository.existsByUserAndStatusAndDueDateBefore(
                user, LoanStatus.ACTIVE, LocalDate.now()
        );

        if (hasOverdueBooks) {
            throw new BadRequestException("User has overdue books and cannot rent new ones.");
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new NotFoundException("Book not found with ID: " + request.getBookId()));

        if (book.getStock() <= 0) {
            throw new BadRequestException("There is no stock available for this book.");
        }
        book.setStock(book.getStock() - 1);
        bookRepository.save(book);

        Loan loan = Loan.builder()
                .user(user)
                .book(book)
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(LoanStatus.ACTIVE)
                .build();

        Loan savedLoan = loanRepository.save(loan);

        return LoanResponseDTO.builder()
                .id(savedLoan.getId())
                .bookTitle(book.getTitle())
                .userEmail(user.getEmail())
                .loanDate(savedLoan.getLoanDate())
                .dueDate(savedLoan.getDueDate())
                .status(savedLoan.getStatus().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponseDTO> getMyLoans() {

        User user = getCurrentUser();

        List<Loan> loans = loanRepository.findByUserOrderByLoanDateDesc(user);

        return loans.stream().map(loan -> LoanResponseDTO.builder()
                .id(loan.getId())
                .bookTitle(loan.getBook().getTitle())
                .userEmail(user.getEmail())
                .loanDate(loan.getLoanDate())
                .dueDate(loan.getDueDate())
                .returnDate(loan.getReturnDate())
                .status(loan.getStatus().name())
                .build()
        ).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<LoanResponseDTO> getAllLoans(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Loan> loanPage = loanRepository.findAll(pageable);

        List<LoanResponseDTO> content = loanPage.getContent().stream().map(loan -> LoanResponseDTO.builder()
                .id(loan.getId())
                .bookTitle(loan.getBook().getTitle())
                .userEmail(loan.getUser().getEmail())
                .loanDate(loan.getLoanDate())
                .dueDate(loan.getDueDate())
                .returnDate(loan.getReturnDate())
                .status(loan.getStatus().name())
                .build()
        ).toList();

        return PaginatedResponseDTO.<LoanResponseDTO>builder()
                .content(content)
                .pageNo(loanPage.getNumber())
                .pageSize(loanPage.getSize())
                .totalElements(loanPage.getTotalElements())
                .totalPages(loanPage.getTotalPages())
                .last(loanPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public LoanResponseDTO returnLoan(Long loanId) {

        User user = getCurrentUser();
        String currentUserEmail = user.getEmail();

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new NotFoundException("Loan not found with ID: " + loanId));

        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isAdmin && !loan.getUser().getEmail().equals(currentUserEmail)) {
            throw new ForbiddenException("You do not have permission to return a book that is not yours.");
        }

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BadRequestException("This loan is already returned.");
        }

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDate.now());

        Book book = loan.getBook();
        book.setStock(book.getStock() + 1);
        bookRepository.save(book);

        Loan savedLoan = loanRepository.save(loan);

        return LoanResponseDTO.builder()
                .id(savedLoan.getId())
                .bookTitle(book.getTitle())
                .userEmail(loan.getUser().getEmail())
                .loanDate(savedLoan.getLoanDate())
                .dueDate(savedLoan.getDueDate())
                .returnDate(savedLoan.getReturnDate())
                .status(savedLoan.getStatus().name())
                .build();
    }

    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new ForbiddenException("User is not properly authenticated.");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found in database"));
    }

}