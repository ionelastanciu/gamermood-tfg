package com.gamermood.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    private static final Logger log = LoggerFactory.getLogger(GroqService.class);

    private final ObjectMapper objectMapper;

    @Value("${groq.api-key:}")
    private String apiKey;

    @Value("${groq.api-url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${groq.max-tokens:500}")
    private int maxTokens;

    public GroqService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String generarRecomendacion(String juego, String mood, int intensidad, String descripcion) {
        return generarRecomendacion(juego, mood, intensidad, descripcion, false);
    }

    public String regenerarRecomendacion(String juego, String mood, int intensidad, String descripcion) {
        return generarRecomendacion(juego, mood, intensidad, descripcion, true);
    }

    private String generarRecomendacion(String juego, String mood, int intensidad, String descripcion, boolean alternativa) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("GROQ_API_KEY no configurada. Usando recomendaciones por reglas.");
            return null;
        }

        try {
            log.info("Intentando generar recomendación con Groq. modelo={}, maxTokens={}", model, maxTokens);

            String prompt = construirPrompt(juego, mood, intensidad, descripcion, alternativa);
            String cuerpoJson = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "max_tokens", maxTokens,
                    "temperature", alternativa ? 0.9 : 0.7,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "Eres un asistente de bienestar digital y videojuegos. Da consejos breves, empáticos y útiles."
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", prompt
                            )
                    )
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpoJson))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            log.info("Respuesta Groq recibida. status={}", response.statusCode());

            if (response.statusCode() != 200) {
                log.warn("Groq devolvió código {}. Se usan reglas como fallback. body={}",
                        response.statusCode(), resumir(response.body()));
                return null;
            }

            String texto = extraerTextoRespuesta(response.body());
            if (texto == null || texto.isBlank()) {
                log.warn("Groq respondió 200, pero no se encontró choices[0].message.content. Se usan reglas.");
                return null;
            }

            log.info("Recomendación Groq generada correctamente.");
            return texto;

        } catch (Exception e) {
            log.warn("No se pudo generar recomendación con Groq. Se usan reglas como fallback: {}", e.getMessage());
            return null;
        }
    }

    private String construirPrompt(String juego, String mood, int intensidad, String descripcion, boolean alternativa) {
        String base = ("He jugado a %s. Mi estado de ánimo era '%s' con una intensidad de %d sobre 10. " +
                "Descripción: %s. Dame 3 recomendaciones breves y concretas para mi próxima sesión.")
                .formatted(juego, mood, intensidad, descripcion != null ? descripcion : "sin descripción");

        if (!alternativa) {
            return base;
        }

        return base + " El usuario ha pedido una nueva recomendación porque la anterior no le resultó útil. " +
                "Propón una alternativa diferente, evitando repetir consejos genéricos o demasiado parecidos.";
    }

    private String extraerTextoRespuesta(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        return root.path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText(null);
    }

    private String resumir(String body) {
        if (body == null || body.isBlank()) {
            return "<sin cuerpo>";
        }
        String limpio = body.replaceAll("\\s+", " ").trim();
        return limpio.length() <= 300 ? limpio : limpio.substring(0, 300) + "...";
    }
}
