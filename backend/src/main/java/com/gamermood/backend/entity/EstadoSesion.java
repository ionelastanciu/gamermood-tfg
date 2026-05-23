package com.gamermood.backend.entity;

/**
 * Estados disponibles para el historial interno de transiciones.
 */
public enum EstadoSesion {
    SESSION_CREATED,
    CLASSIFIED,
    RECOMMENDATION_GENERATED,
    FEEDBACK_RECEIVED,
    CLOSED,
    RETRY_REQUESTED
}
