package com.gamermood.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Integración opcional con OpenAI.
 *
 * En la configuración actual del proyecto no se usa OpenAI por requerir facturación.
 * Si no hay API key o la llamada falla, el servicio devuelve null y el flujo continúa
 * con las recomendaciones por reglas definidas en RecomendacionService.
 */
@Service
public class OpenAiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.max-tokens:500}")
    private int maxTokens;

    public String generarRecomendacion(String juego, String mood, int intensidad, String descripcion) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OPENAI_API_KEY no configurada. Usando sistema de reglas como fallback.");
            return null;
        }

        try {
            String prompt = construirPrompt(juego, mood, intensidad, descripcion);
            String cuerpoJson = """
                    {
                      "model": "%s",
                      "max_tokens": %d,
                      "messages": [
                        {
                          "role": "system",
                          "content": "Eres un asistente experto en bienestar digital y videojuegos. Da consejos breves, empáticos y útiles."
                        },
                        {
                          "role": "user",
                          "content": "%s"
                        }
                      ]
                    }
                    """.formatted(model, maxTokens, escaparJson(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpoJson))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return extraerTextoRespuesta(response.body());
            } else {
                log.error("OpenAI devolvió código {}: {}", response.statusCode(), response.body());
                return null;
            }

        } catch (Exception e) {
            log.error("Error al llamar a OpenAI: {}", e.getMessage());
            return null;
        }
    }

    private String construirPrompt(String juego, String mood, int intensidad, String descripcion) {
        return ("He jugado a %s. Mi estado de ánimo era '%s' con una intensidad de %d sobre 10. " +
                "Descripción: %s. Dame una recomendación breve y útil sobre mi sesión.")
                .formatted(juego, mood, intensidad, descripcion != null ? descripcion : "sin descripción");
    }

    /**
     * Extrae choices[0].message.content sin añadir una dependencia JSON solo para esta llamada opcional.
     */
    private String extraerTextoRespuesta(String json) {
        int inicio = json.indexOf("\"content\":") + 11;
        int fin = json.indexOf("\"", inicio);
        if (inicio > 10 && fin > inicio) {
            return json.substring(inicio, fin).replace("\\n", "\n");
        }
        return null;
    }

    private String escaparJson(String texto) {
        return texto.replace("\"", "\\\"").replace("\n", "\\n");
    }
}
