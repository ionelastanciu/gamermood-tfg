package com.gamermood.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recomendaciones")
public class Recomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_id", nullable = false, unique = true)
    private GameSession sesion;

    @Column(nullable = false, length = 1000)
    private String texto;

    // Fuente: REGLAS (sin IA) | OPENAI (con IA)
    @Column(nullable = false, length = 20)
    private String fuente = "REGLAS";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters y setters
    public Long getId() { return id; }
    public GameSession getSesion() { return sesion; }
    public void setSesion(GameSession sesion) { this.sesion = sesion; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public String getFuente() { return fuente; }
    public void setFuente(String fuente) { this.fuente = fuente; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
