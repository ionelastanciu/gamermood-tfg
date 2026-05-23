package com.gamermood.backend.dto;

import java.time.LocalDateTime;

public record RecomendacionResponseDto(
        Long id,
        Long sesionId,
        String texto,
        String fuente,
        LocalDateTime createdAt
) {}
