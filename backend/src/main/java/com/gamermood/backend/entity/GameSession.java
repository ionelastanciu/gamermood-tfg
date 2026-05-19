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

    @Column(nullable = false, length = 50)
    private String mood;

    @Column(nullable = false)
    private Integer intensity;

    @Column(length = 500)
    private String experience;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "sesion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Recomendacion recomendacion;

    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.Set<TransicionEstado> transiciones = new java.util.HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

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
    public Recomendacion getRecomendacion() { return recomendacion; }
    public void setRecomendacion(Recomendacion recomendacion) { this.recomendacion = recomendacion; }
    public java.util.Set<TransicionEstado> getTransiciones() { return transiciones; }
    public void setTransiciones(java.util.Set<TransicionEstado> transiciones) { this.transiciones = transiciones; }
}
