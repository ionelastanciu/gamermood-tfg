package com.gamermood.backend.service;

import com.gamermood.backend.dto.SessionRequestDto;
import com.gamermood.backend.dto.SessionResponseDto;

import java.util.List;

/**
 * TODO (Florin): implementar cuando las entidades JPA de sesion_juego estén creadas.
 * Pasos:
 *   1. Inyectar repositorio de SesionJuego.
 *   2. createSession(): mapear DTO → entidad, asociar usuario autenticado, guardar.
 *   3. getSessions(): devolver todas las sesiones del usuario autenticado.
 */
public interface SessionService {

    SessionResponseDto createSession(Long userId, SessionRequestDto dto);

    List<SessionResponseDto> getSessions(Long userId);
}
