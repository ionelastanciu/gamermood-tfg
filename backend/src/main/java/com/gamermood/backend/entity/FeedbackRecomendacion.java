package com.gamermood.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_recomendacion")
public class FeedbackRecomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recomendacion_id", nullable = false, unique = true)
    private Recomendacion recomendacion;

    // true = "Me ha servido", false = "No me ha servido"
    @Column(nullable = false)
    private boolean util;

    @Column(length = 500)
    private String comentario;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters y setters
    public Long getId() { return id; }
    public Recomendacion getRecomendacion() { return recomendacion; }
    public void setRecomendacion(Recomendacion recomendacion) { this.recomendacion = recomendacion; }
    public boolean isUtil() { return util; }
    public void setUtil(boolean util) { this.util = util; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
