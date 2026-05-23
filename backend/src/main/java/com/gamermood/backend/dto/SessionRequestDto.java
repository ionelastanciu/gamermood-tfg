package com.gamermood.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SessionRequestDto(
    @NotBlank String game,
    @NotBlank String mood,
    @NotNull @Min(1) @Max(10) Integer intensity,
    String experience
) {}
