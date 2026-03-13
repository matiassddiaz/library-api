package com.matias.library.service;

import com.matias.library.dto.AuthResponseDTO;
import com.matias.library.dto.LoginRequestDTO;
import com.matias.library.dto.RegisterRequestDTO;
import com.matias.library.exception.ConflictException;
import com.matias.library.model.User;
import com.matias.library.model.enums.Role;
import com.matias.library.repository.UserRepository;
import com.matias.library.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // ─── register ─────────────────────────────────────────────────────────────

    @Test
    void register_WhenValidRequest_ShouldSaveUserAndReturnToken() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .firstName("Matias")
                .lastName("Tester")
                .email("matias@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt-token");

        AuthResponseDTO response = authService.register(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("Matias", savedUser.getFirstName());
        assertEquals("matias@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(Role.CLIENT, savedUser.getRole());
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldThrowConflictException() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .email("existing@example.com")
                .build();

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(ConflictException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    // ─── login ────────────────────────────────────────────────────────────────

    @Test
    void login_WhenCredentialsAreValid_ShouldReturnToken() {
        LoginRequestDTO request = new LoginRequestDTO("matias@example.com", "password123");
        User user = User.builder().email("matias@example.com").build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");

        AuthResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());

        verify(authenticationManager).authenticate(
                argThat(auth -> auth.getPrincipal().equals("matias@example.com") &&
                        auth.getCredentials().equals("password123"))
        );
    }

    @Test
    void login_WhenUserNotFoundAfterAuthentication_ShouldThrowUsernameNotFoundException() {
        LoginRequestDTO request = new LoginRequestDTO("wrong@example.com", "password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_WhenCredentialsAreInvalid_ShouldThrowBadCredentialsException() {
        LoginRequestDTO request = new LoginRequestDTO("matias@example.com", "wrongpassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any());
    }
}