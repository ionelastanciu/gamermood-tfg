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
    private final GroqService groqService;

    public RecomendacionService(
            RecomendacionRepository recomendacionRepository,
            SessionRepository sessionRepository,
            GroqService groqService
    ) {
        this.recomendacionRepository = recomendacionRepository;
        this.sessionRepository = sessionRepository;
        this.groqService = groqService;
    }

    @Transactional
    public RecomendacionResponseDto generarParaSesion(Long sesionId, Long userId) {

        GameSession sesion = sessionRepository.findByIdAndUsuarioId(sesionId, userId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Sesión no encontrada"));

        return recomendacionRepository.findBySesionId(sesionId)
                .map(this::toDto)
                .orElseGet(() -> {

                    String texto = groqService.generarRecomendacion(
                            sesion.getGame(),
                            sesion.getMood(),
                            sesion.getIntensity(),
                            sesion.getExperience()
                    );

                    String fuente = texto != null ? "GROQ" : "REGLAS";

                    if (texto == null) {
                        texto = generarTexto(
                                sesion.getMood(),
                                sesion.getIntensity()
                        );
                    }

                    Recomendacion recomendacion = new Recomendacion();
                    recomendacion.setSesion(sesion);
                    recomendacion.setTexto(texto);
                    recomendacion.setFuente(fuente);

                    return toDto(recomendacionRepository.save(recomendacion));
                });
    }

    @Transactional
    public RecomendacionResponseDto regenerarParaSesion(Long sesionId, Long userId) {

        GameSession sesion = sessionRepository.findByIdAndUsuarioId(sesionId, userId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Sesión no encontrada"));

        recomendacionRepository.findBySesionId(sesionId)
                .ifPresent(existente -> {
                    recomendacionRepository.delete(existente);
                });

        String texto = groqService.generarRecomendacion(
                sesion.getGame(),
                sesion.getMood(),
                sesion.getIntensity(),
                sesion.getExperience()
        );

        String fuente = texto != null ? "GROQ" : "REGLAS";

        if (texto == null) {
            texto = generarTextoAlternativo(
                    sesion.getMood(),
                    sesion.getIntensity()
            );
        }

        Recomendacion nueva = new Recomendacion();
        nueva.setSesion(sesion);
        nueva.setTexto(texto);
        nueva.setFuente(fuente);

        return toDto(recomendacionRepository.save(nueva));
    }

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

            default ->
                    "Gracias por registrar tu sesión. Sigue jugando con moderación.";
        };
    }

    private String generarTextoAlternativo(String mood, int intensidad) {

        return switch (mood.toLowerCase()) {

            case "happy", "excited" -> intensidad >= 7
                    ? "Excelente energía. Aprovecha para entrenar técnica en lugar de jugar competitivo."
                    : "Tu buen humor es ideal para probar géneros que nunca hayas explorado.";

            case "neutral" -> intensidad >= 7
                    ? "Sesión larga con estado tranquilo. Hidrátate y planifica la próxima con objetivos claros."
                    : "Un estado neutro puede mejorar añadiendo música animada mientras juegas.";

            case "sad" -> intensidad >= 7
                    ? "Llevas mucho tiempo jugando con el ánimo bajo. Salir a caminar 10 minutos puede ayudar."
                    : "Los juegos narrativos de bajo estrés son perfectos para tu estado actual.";

            case "angry" -> intensidad >= 7
                    ? "Deja el mando por un momento. Escucha música, toma agua y vuelve más tarde."
                    : "Cambia a un juego diferente o en solitario para bajar el nivel de activación.";

            default ->
                    "Registrar tus sesiones te ayuda a mejorar. ¡Sigue así!";
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
