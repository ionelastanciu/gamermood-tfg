package com.gamermood.backend.dto;

import java.time.LocalDateTime;

public record FeedbackResponseDto(
        Long id,
        Long recomendacionId,
        boolean util,
        String comentario,
        LocalDateTime createdAt
) {}
