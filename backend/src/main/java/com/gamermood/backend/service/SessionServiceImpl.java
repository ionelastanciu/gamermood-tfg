package com.gamermood.backend.service;

import com.gamermood.backend.dto.SessionRequestDto;
import com.gamermood.backend.dto.SessionResponseDto;
import com.gamermood.backend.entity.GameSession;
import com.gamermood.backend.entity.User;
import com.gamermood.backend.exception.RecursoNoEncontradoException;
import com.gamermood.backend.repository.SessionRepository;
import com.gamermood.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SessionServiceImpl(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public SessionResponseDto createSession(Long userId, SessionRequestDto dto) {
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        GameSession sesion = new GameSession();
        sesion.setUsuario(usuario);
        sesion.setGame(dto.game());
        sesion.setMood(dto.mood());
        sesion.setIntensity(dto.intensity());
        sesion.setExperience(dto.experience());

        GameSession guardada = sessionRepository.save(sesion);
        return toDto(guardada);
    }

    @Override
    public List<SessionResponseDto> getSessions(Long userId) {
        return sessionRepository.findByUsuarioIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public SessionResponseDto getSessionById(Long userId, Long sessionId) {
        GameSession sesion = sessionRepository.findByIdAndUsuarioId(sessionId, userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión no encontrada"));
        return toDto(sesion);
    }

    @Override
    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        GameSession sesion = sessionRepository.findByIdAndUsuarioId(sessionId, userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión no encontrada"));
        sessionRepository.delete(sesion);
    }

    private SessionResponseDto toDto(GameSession sesion) {
        return new SessionResponseDto(
                sesion.getId(),
                sesion.getGame(),
                sesion.getMood(),
                sesion.getIntensity(),
                sesion.getExperience(),
                sesion.getCreatedAt()
        );
    }
}
