package com.matias.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matias.library.dto.AuthResponseDTO;
import com.matias.library.dto.LoginRequestDTO;
import com.matias.library.dto.RegisterRequestDTO;
import com.matias.library.exception.ConflictException;
import com.matias.library.security.JwtService;
import com.matias.library.service.IAuthService;
import com.matias.library.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─── POST /api/auth/register ──────────────────────────────────────────────

    @Test
    void register_WhenValid_ShouldReturn201() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .firstName("Matias")
                .lastName("Perez")
                .email("matias@example.com")
                .password("password123")
                .build();
        AuthResponseDTO response = new AuthResponseDTO("generated-token");

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("generated-token"));
    }

    @Test
    void register_WhenEmailIsInvalid_ShouldReturn400() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .firstName("Matias")
                .lastName("Perez")
                .email("invalid-email")
                .password("123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldThrowConflictException() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .firstName("Matias")
                .lastName("Perez")
                .email("existing@example.com")
                .password("password123")
                .build();

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new ConflictException("Account already existing with email"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ─── POST /api/auth/login ─────────────────────────────────────────────────

    @Test
    void login_WhenValid_ShouldReturn200() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("matias@example.com", "password123");
        AuthResponseDTO response = new AuthResponseDTO("login-token");

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("login-token"));
    }

    @Test
    void login_WhenCredentialsEmpty_ShouldReturn400() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WhenBadCredentials_ShouldThrowBadCredentialsException() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("matias@example.com", "wrongpassword");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_WhenUserNotFound_ShouldThrowUsernameNotFoundException() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("notfound@example.com", "password123");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}