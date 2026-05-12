package com.gamermood.backend.dto;

import java.util.List;

public record AuthResponseDto(
        String token,
        String refreshToken,
        Long userId,
        String username,
        String email,
        List<String> roles
) {}
