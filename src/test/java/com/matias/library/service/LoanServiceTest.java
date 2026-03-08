package com.matias.library.service;

import com.matias.library.dto.LoanRequestDTO;
import com.matias.library.dto.LoanResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.exception.BadRequestException;
import com.matias.library.exception.ForbiddenException;
import com.matias.library.exception.NotFoundException;
import com.matias.library.model.Book;
import com.matias.library.model.Loan;
import com.matias.library.model.User;
import com.matias.library.model.enums.LoanStatus;
import com.matias.library.model.enums.Role;
import com.matias.library.repository.BookRepository;
import com.matias.library.repository.LoanRepository;
import com.matias.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoanService service;

    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    private User clientUser;
    private User adminUser;
    private Book availableBook;
    private Book rentedBook;

    @BeforeEach
    void setUp() {
        clientUser = User.builder()
                .id(1L)
                .email("client@test.com")
                .role(Role.CLIENT)
                .build();

        adminUser = User.builder()
                .id(2L)
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build();

        availableBook = Book.builder()
                .id(10L)
                .title("1984")
                .rented(false)
                .build();

        rentedBook = Book.builder()
                .id(11L)
                .title("Brave New World")
                .rented(true)
                .build();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private void mockAuthenticatedUser(User user) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(user.getEmail());
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    // ─── createLoan ───────────────────────────────────────────────────────────

    @Test
    void createLoan_WhenValid_ShouldCreateAndReturnDto() {
        mockAuthenticatedUser(clientUser);

        LoanRequestDTO request = LoanRequestDTO.builder().bookId(10L).build();

        when(loanRepository.existsByUserAndStatusAndDueDateBefore(
                eq(clientUser), eq(LoanStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(false);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(availableBook));

        Loan savedLoan = Loan.builder()
                .id(100L)
                .user(clientUser)
                .book(availableBook)
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(LoanStatus.ACTIVE)
                .build();
        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);

        LoanResponseDTO result = service.createLoan(request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("1984", result.getBookTitle());
        assertEquals("client@test.com", result.getUserEmail());
        assertEquals(LoanStatus.ACTIVE.name(), result.getStatus());
        assertTrue(availableBook.isRented());
        verify(bookRepository).save(availableBook);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void createLoan_WhenUserHasOverdueBooks_ShouldThrowBadRequestException() {
        mockAuthenticatedUser(clientUser);

        LoanRequestDTO request = LoanRequestDTO.builder().bookId(10L).build();

        when(loanRepository.existsByUserAndStatusAndDueDateBefore(
                eq(clientUser), eq(LoanStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.createLoan(request));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void createLoan_WhenBookNotFound_ShouldThrowNotFoundException() {
        mockAuthenticatedUser(clientUser);

        LoanRequestDTO request = LoanRequestDTO.builder().bookId(99L).build();

        when(loanRepository.existsByUserAndStatusAndDueDateBefore(
                eq(clientUser), eq(LoanStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(false);
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.createLoan(request));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void createLoan_WhenBookAlreadyRented_ShouldThrowBadRequestException() {
        mockAuthenticatedUser(clientUser);

        LoanRequestDTO request = LoanRequestDTO.builder().bookId(11L).build();

        when(loanRepository.existsByUserAndStatusAndDueDateBefore(
                eq(clientUser), eq(LoanStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(false);
        when(bookRepository.findById(11L)).thenReturn(Optional.of(rentedBook));

        assertThrows(BadRequestException.class, () -> service.createLoan(request));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void createLoan_WhenUserNotAuthenticated_ShouldThrowForbiddenException() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        LoanRequestDTO request = LoanRequestDTO.builder().bookId(10L).build();

        assertThrows(ForbiddenException.class, () -> service.createLoan(request));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void createLoan_WhenUserNotFoundInDb_ShouldThrowNotFoundException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("ghost@test.com");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        LoanRequestDTO request = LoanRequestDTO.builder().bookId(10L).build();

        assertThrows(NotFoundException.class, () -> service.createLoan(request));
    }

    // ─── getMyLoans ───────────────────────────────────────────────────────────

    @Test
    void getMyLoans_WhenUserHasLoans_ShouldReturnList() {
        mockAuthenticatedUser(clientUser);

        Loan loan = Loan.builder()
                .id(1L)
                .user(clientUser)
                .book(availableBook)
                .loanDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(9))
                .status(LoanStatus.ACTIVE)
                .build();

        when(loanRepository.findByUserOrderByLoanDateDesc(clientUser)).thenReturn(List.of(loan));

        List<LoanResponseDTO> result = service.getMyLoans();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1984", result.get(0).getBookTitle());
        assertEquals("client@test.com", result.get(0).getUserEmail());
        assertEquals(LoanStatus.ACTIVE.name(), result.get(0).getStatus());
    }

    @Test
    void getMyLoans_WhenUserHasNoLoans_ShouldReturnEmptyList() {
        mockAuthenticatedUser(clientUser);

        when(loanRepository.findByUserOrderByLoanDateDesc(clientUser)).thenReturn(List.of());

        List<LoanResponseDTO> result = service.getMyLoans();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─── getAllLoans ──────────────────────────────────────────────────────────

    @Test
    void getAllLoans_ShouldReturnPaginatedResponse() {
        Loan loan = Loan.builder()
                .id(1L)
                .user(clientUser)
                .book(availableBook)
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(LoanStatus.ACTIVE)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> page = new PageImpl<>(List.of(loan), pageable, 1);

        when(loanRepository.findAll(pageable)).thenReturn(page);

        PaginatedResponseDTO<LoanResponseDTO> result = service.getAllLoans(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(0, result.getPageNo());
        assertTrue(result.isLast());
        assertEquals("1984", result.getContent().get(0).getBookTitle());
    }

    @Test
    void getAllLoans_WhenEmpty_ShouldReturnEmptyContent() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(loanRepository.findAll(pageable)).thenReturn(emptyPage);

        PaginatedResponseDTO<LoanResponseDTO> result = service.getAllLoans(0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    // ─── returnLoan ───────────────────────────────────────────────────────────

    @Test
    void returnLoan_WhenUserReturnsOwnBook_ShouldReturnDto() {
        mockAuthenticatedUser(clientUser);

        Loan loan = Loan.builder()
                .id(50L)
                .user(clientUser)
                .book(rentedBook)
                .loanDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(9))
                .status(LoanStatus.ACTIVE)
                .build();

        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        LoanResponseDTO result = service.returnLoan(50L);

        assertNotNull(result);
        assertEquals(LoanStatus.RETURNED.name(), result.getStatus());
        assertNotNull(result.getReturnDate());
        assertFalse(rentedBook.isRented());
        verify(bookRepository).save(rentedBook);
        verify(loanRepository).save(loan);
    }

    @Test
    void returnLoan_WhenAdminReturnsAnyBook_ShouldReturnDto() {
        mockAuthenticatedUser(adminUser);

        Loan loan = Loan.builder()
                .id(50L)
                .user(clientUser)
                .book(rentedBook)
                .loanDate(LocalDate.now().minusDays(3))
                .dueDate(LocalDate.now().plusDays(11))
                .status(LoanStatus.ACTIVE)
                .build();

        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        LoanResponseDTO result = service.returnLoan(50L);

        assertNotNull(result);
        assertEquals(LoanStatus.RETURNED.name(), result.getStatus());
    }

    @Test
    void returnLoan_WhenUserTriesToReturnAnotherUsersBook_ShouldThrowForbiddenException() {
        User otherUser = User.builder().id(99L).email("other@test.com").role(Role.CLIENT).build();
        mockAuthenticatedUser(otherUser);

        Loan loan = Loan.builder()
                .id(50L)
                .user(clientUser)
                .book(rentedBook)
                .status(LoanStatus.ACTIVE)
                .build();

        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));

        assertThrows(ForbiddenException.class, () -> service.returnLoan(50L));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void returnLoan_WhenLoanAlreadyReturned_ShouldThrowBadRequestException() {
        mockAuthenticatedUser(clientUser);

        Loan alreadyReturnedLoan = Loan.builder()
                .id(50L)
                .user(clientUser)
                .book(availableBook)
                .loanDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().minusDays(3))
                .returnDate(LocalDate.now().minusDays(3))
                .status(LoanStatus.RETURNED)
                .build();

        when(loanRepository.findById(50L)).thenReturn(Optional.of(alreadyReturnedLoan));

        assertThrows(BadRequestException.class, () -> service.returnLoan(50L));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void returnLoan_WhenLoanNotFound_ShouldThrowNotFoundException() {
        mockAuthenticatedUser(clientUser);

        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.returnLoan(99L));
        verify(loanRepository, never()).save(any());
    }
}