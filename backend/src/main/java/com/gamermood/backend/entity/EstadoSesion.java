package com.gamermood.backend.entity;

/**
 * Estados por los que pasa una sesión de juego en GamerMood.
 *
 * Flujo normal:
 *   SESSION_CREATED → CLASSIFIED → RECOMMENDATION_GENERATED → FEEDBACK_RECEIVED → CLOSED
 *
 * Flujo alternativo:
 *   Cualquier estado → RETRY_REQUESTED → RECOMMENDATION_GENERATED
 */
public enum EstadoSesion {
    SESSION_CREATED,          // La sesión acaba de registrarse
    CLASSIFIED,               // El mood e intensidad han sido procesados
    RECOMMENDATION_GENERATED, // Se ha generado una recomendación
    FEEDBACK_RECEIVED,        // El usuario ha dado feedback
    CLOSED,                   // El ciclo ha terminado
    RETRY_REQUESTED           // El usuario pide otra recomendación
}
