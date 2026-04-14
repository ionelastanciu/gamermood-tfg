package com.gamermood.backend.dto;

import java.util.List;

public record AuthResponseDto(
    String token,
    Long userId,
    String username,
    String email,
    List<String> roles
) {}
