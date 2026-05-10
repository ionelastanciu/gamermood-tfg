package com.gamermood.backend.dto;

import jakarta.validation.constraints.NotNull;

public record FeedbackRequestDto(

        @NotNull(message = "Debes indicar si la recomendación te ha sido útil")
        Boolean util,

        String comentario

) {}
