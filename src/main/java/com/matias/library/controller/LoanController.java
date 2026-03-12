package com.matias.library.controller;

import com.matias.library.dto.LoanRequestDTO;
import com.matias.library.dto.LoanResponseDTO;
import com.matias.library.service.ILoanService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final ILoanService service;

    @PostMapping
    @ApiResponse(responseCode = "201", description = "Loan successfully created")
    public ResponseEntity<LoanResponseDTO> createLoan(@Valid @RequestBody LoanRequestDTO request) {
        LoanResponseDTO response = service.createLoan(request);
        URI location = URI.create("/api/loans/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<LoanResponseDTO>> getMyLoans() {
        return ResponseEntity.ok(service.getMyLoans());
    }

    @GetMapping
    public ResponseEntity<com.matias.library.dto.PaginatedResponseDTO<LoanResponseDTO>> getAllLoans(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size) {
        return ResponseEntity.ok(service.getAllLoans(page, size));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<LoanResponseDTO> returnLoan(@PathVariable Long id) {
        LoanResponseDTO response = service.returnLoan(id);
        return ResponseEntity.ok(response);
    }

}