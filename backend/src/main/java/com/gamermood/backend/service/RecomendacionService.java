package com.gamermood.backend.service;

import com.gamermood.backend.dto.RecomendacionResponseDto;
import com.gamermood.backend.entity.GameSession;
import com.gamermood.backend.entity.Recomendacion;
import com.gamermood.backend.exception.RecursoNoEncontradoException;
import com.gamermood.backend.repository.RecomendacionRepository;
import com.gamermood.backend.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecomendacionService {

    private final RecomendacionRepository recomendacionRepository;
    private final SessionRepository sessionRepository;

    public RecomendacionService(RecomendacionRepository recomendacionRepository,
                                SessionRepository sessionRepository) {
        this.recomendacionRepository = recomendacionRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public RecomendacionResponseDto generarParaSesion(Long sesionId, Long userId) {
        // Comprobamos que la sesión pertenece al usuario
        GameSession sesion = sessionRepository.findByIdAndUsuarioId(sesionId, userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión no encontrada"));

        // Si ya tiene recomendación, la devolvemos
        return recomendacionRepository.findBySesionId(sesionId)
                .map(this::toDto)
                .orElseGet(() -> {
                    Recomendacion rec = new Recomendacion();
                    rec.setSesion(sesion);
                    rec.setTexto(generarTexto(sesion.getMood(), sesion.getIntensity()));
                    rec.setFuente("REGLAS");
                    return toDto(recomendacionRepository.save(rec));
                });
    }

    /**
     * Reglas simples según mood e intensidad.
     * Preparado para sustituir por llamada a OpenAI en el futuro.
     */
    private String generarTexto(String mood, int intensidad) {
        return switch (mood.toLowerCase()) {
            case "happy", "excited" -> intensidad >= 7
                    ? "¡Gran sesión! Considera jugar con otros para mantener esa energía positiva."
                    : "Buen estado de ánimo. Puedes explorar juegos nuevos mientras estés motivado.";
            case "neutral" -> intensidad >= 7
                    ? "Sesión intensa con estado neutro. Descansa un poco antes de la siguiente."
                    : "Todo tranquilo. Un juego relajado puede mejorar tu humor.";
            case "sad" -> intensidad >= 7
                    ? "Has jugado mucho estando bajo de ánimo. Tómate un descanso y habla con alguien."
                    : "Si estás triste, prueba juegos cooperativos o de historia para desconectar.";
            case "angry" -> intensidad >= 7
                    ? "Alta intensidad con mal humor: para y respira antes de otra sesión."
                    : "Prueba juegos de puzzles o estrategia lenta para calmarte.";
            default -> "Gracias por registrar tu sesión. Sigue jugando con moderación.";
        };
    }

    private RecomendacionResponseDto toDto(Recomendacion rec) {
        return new RecomendacionResponseDto(
                rec.getId(),
                rec.getSesion().getId(),
                rec.getTexto(),
                rec.getFuente(),
                rec.getCreatedAt()
        );
    }
}
