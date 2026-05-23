package com.gamermood.backend.controller;

import com.gamermood.backend.dto.RecomendacionResponseDto;
import com.gamermood.backend.repository.UserRepository;
import com.gamermood.backend.service.RecomendacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
public class RecomendacionController {

    private final RecomendacionService recomendacionService;
    private final UserRepository userRepository;

    public RecomendacionController(RecomendacionService recomendacionService,
                                   UserRepository userRepository) {
        this.recomendacionService = recomendacionService;
        this.userRepository = userRepository;
    }

    @PostMapping("/{sesionId}")
    public ResponseEntity<RecomendacionResponseDto> generar(
            @PathVariable Long sesionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = resolverUserId(userDetails);
        return ResponseEntity.ok(recomendacionService.generarParaSesion(sesionId, userId));
    }

    @PostMapping("/{sesionId}/retry")
    public ResponseEntity<RecomendacionResponseDto> retry(
            @PathVariable Long sesionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = resolverUserId(userDetails);
        return ResponseEntity.ok(recomendacionService.regenerarParaSesion(sesionId, userId));
    }

    private Long resolverUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getId();
    }
}
