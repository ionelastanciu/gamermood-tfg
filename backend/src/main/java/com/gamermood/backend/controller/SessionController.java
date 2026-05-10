package com.gamermood.backend.controller;

import com.gamermood.backend.dto.SessionRequestDto;
import com.gamermood.backend.dto.SessionResponseDto;
import com.gamermood.backend.repository.UserRepository;
import com.gamermood.backend.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    public SessionController(SessionService sessionService, UserRepository userRepository) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<SessionResponseDto> crear(
            @Valid @RequestBody SessionRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = resolverUserId(userDetails);
        SessionResponseDto respuesta = sessionService.createSession(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<SessionResponseDto>> listar(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = resolverUserId(userDetails);
        return ResponseEntity.ok(sessionService.getSessions(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionResponseDto> detalle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = resolverUserId(userDetails);
        return ResponseEntity.ok(sessionService.getSessionById(userId, id));
    }

    private Long resolverUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getId();
    }
}
