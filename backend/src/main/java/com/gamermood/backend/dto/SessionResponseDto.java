package com.gamermood.backend.dto;

import java.time.LocalDateTime;

public record SessionResponseDto(
    Long id,
    String game,
    String mood,
    Integer intensity,
    String experience,
    LocalDateTime createdAt
) {}
