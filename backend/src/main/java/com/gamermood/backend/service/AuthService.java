package com.gamermood.backend.service;

import com.gamermood.backend.dto.AuthResponseDto;
import com.gamermood.backend.dto.LoginRequestDto;
import com.gamermood.backend.dto.RegisterRequestDto;

public interface AuthService {

    void register(RegisterRequestDto dto);

    AuthResponseDto login(LoginRequestDto dto);
}
