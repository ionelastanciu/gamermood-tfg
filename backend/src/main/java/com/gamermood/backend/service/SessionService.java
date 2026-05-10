package com.gamermood.backend.service;

import com.gamermood.backend.dto.SessionRequestDto;
import com.gamermood.backend.dto.SessionResponseDto;

import java.util.List;

public interface SessionService {

    SessionResponseDto createSession(Long userId, SessionRequestDto dto);

    List<SessionResponseDto> getSessions(Long userId);

    SessionResponseDto getSessionById(Long userId, Long sessionId);
}
