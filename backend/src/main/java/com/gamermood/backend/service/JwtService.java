package com.gamermood.backend.service;

import java.util.List;

public interface JwtService {

    String generarToken(String email, Long userId, String username, List<String> roles);

    String generarRefreshToken(String email);

    String extraerEmail(String token);

    boolean esTokenValido(String token, String email);
}
