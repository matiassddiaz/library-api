package com.matias.library.service;

import com.matias.library.dto.AuthResponseDTO;
import com.matias.library.dto.LoginRequestDTO;
import com.matias.library.dto.RegisterRequestDTO;

public interface IAuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
}