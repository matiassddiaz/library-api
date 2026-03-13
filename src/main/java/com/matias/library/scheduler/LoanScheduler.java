package com.matias.library.scheduler;

import com.matias.library.model.Loan;
import com.matias.library.model.enums.LoanStatus;
import com.matias.library.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanScheduler {

    private final LoanRepository loanRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkAndMarkOverdueLoans() {
        log.info("Starting scheduled task: Checking for overdue loans...");

        LocalDate today = LocalDate.now();
        List<Loan> overdueLoans = loanRepository.findByStatusAndDueDateBefore(LoanStatus.ACTIVE, today);

        if (!overdueLoans.isEmpty()) {
            overdueLoans.forEach(loan -> loan.setStatus(LoanStatus.OVERDUE));
            loanRepository.saveAll(overdueLoans);
            log.info("Successfully updated {} loans to OVERDUE status.", overdueLoans.size());
        } else {
            log.info("No overdue loans found for today.");
        }
    }
}