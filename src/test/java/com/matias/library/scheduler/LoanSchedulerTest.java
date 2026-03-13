package com.matias.library.scheduler;

import com.matias.library.model.Book;
import com.matias.library.model.Loan;
import com.matias.library.model.User;
import com.matias.library.model.enums.LoanStatus;
import com.matias.library.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanSchedulerTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanScheduler scheduler;

    // ─── checkAndMarkOverdueLoans ─────────────────────────────────────────────

    @Test
    void checkAndMarkOverdueLoans_WhenOverdueLoansExist_ShouldMarkThemAsOverdue() {
        Loan loan1 = Loan.builder()
                .id(1L)
                .user(User.builder().id(1L).email("a@test.com").build())
                .book(Book.builder().id(10L).title("1984").build())
                .loanDate(LocalDate.now().minusDays(20))
                .dueDate(LocalDate.now().minusDays(6))
                .status(LoanStatus.ACTIVE)
                .build();

        Loan loan2 = Loan.builder()
                .id(2L)
                .user(User.builder().id(2L).email("b@test.com").build())
                .book(Book.builder().id(11L).title("Brave New World").build())
                .loanDate(LocalDate.now().minusDays(30))
                .dueDate(LocalDate.now().minusDays(1))
                .status(LoanStatus.ACTIVE)
                .build();

        when(loanRepository.findByStatusAndDueDateBefore(eq(LoanStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(List.of(loan1, loan2));

        scheduler.checkAndMarkOverdueLoans();

        assertEquals(LoanStatus.OVERDUE, loan1.getStatus());
        assertEquals(LoanStatus.OVERDUE, loan2.getStatus());

        ArgumentCaptor<List<Loan>> captor = ArgumentCaptor.forClass(List.class);
        verify(loanRepository, times(1)).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    void checkAndMarkOverdueLoans_WhenNoOverdueLoans_ShouldNotCallSaveAll() {
        when(loanRepository.findByStatusAndDueDateBefore(eq(LoanStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(List.of());

        scheduler.checkAndMarkOverdueLoans();

        verify(loanRepository, never()).saveAll(any());
    }

    @Test
    void checkAndMarkOverdueLoans_ShouldQueryWithTodayAsDate() {
        LocalDate today = LocalDate.now();

        when(loanRepository.findByStatusAndDueDateBefore(eq(LoanStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(List.of());

        scheduler.checkAndMarkOverdueLoans();

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(loanRepository).findByStatusAndDueDateBefore(eq(LoanStatus.ACTIVE), dateCaptor.capture());
        assertEquals(today, dateCaptor.getValue());
    }

    @Test
    void checkAndMarkOverdueLoans_WhenSingleOverdueLoan_ShouldMarkItAndPersist() {
        Loan loan = Loan.builder()
                .id(5L)
                .user(User.builder().id(1L).email("user@test.com").build())
                .book(Book.builder().id(10L).title("Dune").build())
                .loanDate(LocalDate.now().minusDays(16))
                .dueDate(LocalDate.now().minusDays(2))
                .status(LoanStatus.ACTIVE)
                .build();

        when(loanRepository.findByStatusAndDueDateBefore(eq(LoanStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(List.of(loan));

        scheduler.checkAndMarkOverdueLoans();

        assertEquals(LoanStatus.OVERDUE, loan.getStatus());
        verify(loanRepository, times(1)).saveAll(List.of(loan));
    }
}