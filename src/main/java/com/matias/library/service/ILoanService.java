package com.matias.library.service;

import com.matias.library.dto.LoanRequestDTO;
import com.matias.library.dto.LoanResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;

import java.util.List;

public interface ILoanService {
    LoanResponseDTO createLoan(LoanRequestDTO request);
    List<LoanResponseDTO> getMyLoans();
    PaginatedResponseDTO<LoanResponseDTO> getAllLoans(int page, int size);
    LoanResponseDTO returnLoan(Long loanId);
}