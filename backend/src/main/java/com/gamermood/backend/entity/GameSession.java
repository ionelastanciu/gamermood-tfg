package com.gamermood.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones_juego")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(nullable = false, length = 100)
    private String game;

    // Estado de ánimo: happy, neutral, sad, angry, excited
    @Column(nullable = false, length = 50)
    private String mood;

    // Intensidad de la sesión del 1 al 10
    @Column(nullable = false)
    private Integer intensity;

    // Descripción o experiencia libre del usuario
    @Column(length = 500)
    private String experience;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters y setters
    public Long getId() { return id; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public Integer getIntensity() { return intensity; }
    public void setIntensity(Integer intensity) { this.intensity = intensity; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
