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

    @Column(nullable = false, length = 20)
    private String fuente = "REGLAS";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(
            mappedBy = "recomendacion",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private FeedbackRecomendacion feedback;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public GameSession getSesion() {
        return sesion;
    }

    public void setSesion(GameSession sesion) {
        this.sesion = sesion;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public FeedbackRecomendacion getFeedback() {
        return feedback;
    }

    public void setFeedback(FeedbackRecomendacion feedback) {
        this.feedback = feedback;

        if (feedback != null) {
            feedback.setRecomendacion(this);
        }
    }

    public void removeFeedback() {
        if (this.feedback != null) {
            this.feedback.setRecomendacion(null);
            this.feedback = null;
        }
    }
}