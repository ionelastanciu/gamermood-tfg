package com.gamermood.backend.controller;

import com.gamermood.backend.dto.FeedbackRequestDto;
import com.gamermood.backend.dto.FeedbackResponseDto;
import com.gamermood.backend.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // POST /api/feedback/{recomendacionId}
    // El usuario envía si la recomendación le ha servido y un comentario opcional
    @PostMapping("/{recomendacionId}")
    public ResponseEntity<FeedbackResponseDto> enviar(
            @PathVariable Long recomendacionId,
            @Valid @RequestBody FeedbackRequestDto dto) {

        FeedbackResponseDto respuesta = feedbackService.guardar(recomendacionId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}
