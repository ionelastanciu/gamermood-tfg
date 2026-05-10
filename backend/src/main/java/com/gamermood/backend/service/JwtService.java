package com.gamermood.backend.service;

/**
 * Contrato para generar y validar tokens JWT.
 * La implementación se añade en el paso de configuración JWT.
 */
public interface JwtService {

    String generarToken(String email);

    String extraerEmail(String token);

    boolean esTokenValido(String token, String email);
}
