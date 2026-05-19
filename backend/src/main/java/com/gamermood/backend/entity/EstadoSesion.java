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
    SESSION_CREATED,
    CLASSIFIED,
    RECOMMENDATION_GENERATED,
    FEEDBACK_RECEIVED,
    CLOSED,
    RETRY_REQUESTED
}
