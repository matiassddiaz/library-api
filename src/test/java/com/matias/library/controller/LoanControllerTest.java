package com.matias.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.matias.library.dto.LoanRequestDTO;
import com.matias.library.dto.LoanResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.exception.BadRequestException;
import com.matias.library.exception.ForbiddenException;
import com.matias.library.exception.NotFoundException;
import com.matias.library.security.JwtAuthenticationFilter;
import com.matias.library.security.JwtService;
import com.matias.library.service.ILoanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ILoanService loanService;

    @MockitoBean private JwtService jwtService;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private AuthenticationProvider authenticationProvider;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // ─── helpers ─────────────────────────────────────────────────────────────

    private LoanResponseDTO buildLoanResponse() {
        return LoanResponseDTO.builder()
                .id(1L)
                .bookTitle("1984")
                .userEmail("client@test.com")
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status("ACTIVE")
                .build();
    }

    // ─── POST /api/loans ──────────────────────────────────────────────────────

    @Test
    void createLoan_WhenValid_ShouldReturn201WithBody() throws Exception {
        LoanRequestDTO request = LoanRequestDTO.builder().bookId(10L).build();
        LoanResponseDTO response = buildLoanResponse();

        when(loanService.createLoan(any(LoanRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bookTitle").value("1984"))
                .andExpect(jsonPath("$.userEmail").value("client@test.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createLoan_WhenBookIdIsNull_ShouldReturn400() throws Exception {
        LoanRequestDTO request = LoanRequestDTO.builder().bookId(null).build();

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.bookId").exists());
    }

    @Test
    void createLoan_WhenBookAlreadyRented_ShouldReturn400() throws Exception {
        LoanRequestDTO request = LoanRequestDTO.builder().bookId(10L).build();

        when(loanService.createLoan(any(LoanRequestDTO.class)))
                .thenThrow(new BadRequestException("The book is currently rented and not available."));

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createLoan_WhenUserHasOverdueBooks_ShouldReturn400() throws Exception {
        LoanRequestDTO request = LoanRequestDTO.builder().bookId(10L).build();

        when(loanService.createLoan(any(LoanRequestDTO.class)))
                .thenThrow(new BadRequestException("User has overdue books and cannot rent new ones."));

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createLoan_WhenBookNotFound_ShouldReturn404() throws Exception {
        LoanRequestDTO request = LoanRequestDTO.builder().bookId(99L).build();

        when(loanService.createLoan(any(LoanRequestDTO.class)))
                .thenThrow(new NotFoundException("Book not found with ID: 99"));

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─── GET /api/loans/me ────────────────────────────────────────────────────

    @Test
    void getMyLoans_ShouldReturn200WithList() throws Exception {
        when(loanService.getMyLoans()).thenReturn(List.of(buildLoanResponse()));

        mockMvc.perform(get("/api/loans/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].bookTitle").value("1984"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getMyLoans_WhenNoLoans_ShouldReturn200WithEmptyList() throws Exception {
        when(loanService.getMyLoans()).thenReturn(List.of());

        mockMvc.perform(get("/api/loans/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMyLoans_WhenNotAuthenticated_ShouldReturn403() throws Exception {
        when(loanService.getMyLoans())
                .thenThrow(new ForbiddenException("User is not properly authenticated."));

        mockMvc.perform(get("/api/loans/me"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // ─── GET /api/loans ───────────────────────────────────────────────────────

    @Test
    void getAllLoans_ShouldReturn200WithPaginatedResponse() throws Exception {
        PaginatedResponseDTO<LoanResponseDTO> paginated = PaginatedResponseDTO.<LoanResponseDTO>builder()
                .content(List.of(buildLoanResponse()))
                .pageNo(0).pageSize(10).totalElements(1).totalPages(1).last(true)
                .build();

        when(loanService.getAllLoans(0, 10)).thenReturn(paginated);

        mockMvc.perform(get("/api/loans").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageNo").value(0))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void getAllLoans_WithDefaultParams_ShouldReturn200() throws Exception {
        PaginatedResponseDTO<LoanResponseDTO> paginated = PaginatedResponseDTO.<LoanResponseDTO>builder()
                .content(List.of())
                .pageNo(0).pageSize(10).totalElements(0).totalPages(0).last(true)
                .build();

        when(loanService.getAllLoans(0, 10)).thenReturn(paginated);

        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ─── PATCH /api/loans/{id}/return ─────────────────────────────────────────

    @Test
    void returnLoan_WhenValid_ShouldReturn200WithReturnedStatus() throws Exception {
        LoanResponseDTO response = LoanResponseDTO.builder()
                .id(1L)
                .bookTitle("1984")
                .userEmail("client@test.com")
                .loanDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(9))
                .returnDate(LocalDate.now())
                .status("RETURNED")
                .build();

        when(loanService.returnLoan(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/loans/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnDate").isNotEmpty());
    }

    @Test
    void returnLoan_WhenLoanNotFound_ShouldReturn404() throws Exception {
        when(loanService.returnLoan(99L))
                .thenThrow(new NotFoundException("Loan not found with ID: 99"));

        mockMvc.perform(patch("/api/loans/99/return"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returnLoan_WhenAlreadyReturned_ShouldReturn400() throws Exception {
        when(loanService.returnLoan(1L))
                .thenThrow(new BadRequestException("This loan is already returned."));

        mockMvc.perform(patch("/api/loans/1/return"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returnLoan_WhenNotAuthorized_ShouldReturn403() throws Exception {
        when(loanService.returnLoan(1L))
                .thenThrow(new ForbiddenException("You do not have permission to return a book that is not yours."));

        mockMvc.perform(patch("/api/loans/1/return"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}