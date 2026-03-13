package com.matias.library.repository;

import com.matias.library.model.Loan;
import com.matias.library.model.User;
import com.matias.library.model.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    boolean existsByUserAndStatusAndDueDateBefore(User user, LoanStatus status, LocalDate currentDate);
    boolean existsByUserAndStatus(User user, LoanStatus status);
    List<Loan> findByUserOrderByLoanDateDesc(User user);
    List<Loan> findByStatusAndDueDateBefore(LoanStatus status, LocalDate date);
}
