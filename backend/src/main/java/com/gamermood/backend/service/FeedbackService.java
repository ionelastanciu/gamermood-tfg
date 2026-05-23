package com.gamermood.backend.service;

import com.gamermood.backend.dto.FeedbackRequestDto;
import com.gamermood.backend.dto.FeedbackResponseDto;
import com.gamermood.backend.entity.FeedbackRecomendacion;
import com.gamermood.backend.entity.Recomendacion;
import com.gamermood.backend.exception.RecursoNoEncontradoException;
import com.gamermood.backend.repository.FeedbackRepository;
import com.gamermood.backend.repository.RecomendacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final RecomendacionRepository recomendacionRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           RecomendacionRepository recomendacionRepository) {
        this.feedbackRepository = feedbackRepository;
        this.recomendacionRepository = recomendacionRepository;
    }

    @Transactional
    public FeedbackResponseDto guardar(Long recomendacionId, FeedbackRequestDto dto) {
        Recomendacion recomendacion = recomendacionRepository.findById(recomendacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Recomendación no encontrada"));

        if (feedbackRepository.existsByRecomendacionId(recomendacionId)) {
            throw new IllegalArgumentException("Ya has enviado feedback para esta recomendación");
        }

        FeedbackRecomendacion feedback = new FeedbackRecomendacion();
        feedback.setRecomendacion(recomendacion);
        feedback.setUtil(dto.util());
        feedback.setComentario(dto.comentario());
        recomendacion.setFeedback(feedback);

        FeedbackRecomendacion guardado = feedbackRepository.save(feedback);
        return toDto(guardado);
    }

    private FeedbackResponseDto toDto(FeedbackRecomendacion f) {
        return new FeedbackResponseDto(
                f.getId(),
                f.getRecomendacion().getId(),
                f.isUtil(),
                f.getComentario(),
                f.getCreatedAt()
        );
    }
}
