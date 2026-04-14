package com.gamermood.backend.service;

import com.gamermood.backend.dto.AuthResponseDto;
import com.gamermood.backend.dto.LoginRequestDto;
import com.gamermood.backend.dto.RegisterRequestDto;

/**
 * TODO (Florin): implementar cuando el modelo de usuarios/roles esté validado por Mario.
 * Pasos:
 *   1. Inyectar repositorio de Usuario y codificador de contraseñas.
 *   2. register(): validar unicidad, hashear password, guardar, devolver void.
 *   3. login(): buscar usuario, verificar hash, generar JWT y devolver AuthResponseDto.
 */
public interface AuthService {

    void register(RegisterRequestDto dto);

    AuthResponseDto login(LoginRequestDto dto);
}
