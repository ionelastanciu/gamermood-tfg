package com.gamermood.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transiciones_estado")
public class TransicionEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_id", nullable = false)
    private GameSession sesion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 40)
    private EstadoSesion estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 40)
    private EstadoSesion estadoNuevo;

    @Column(length = 200)
    private String motivo;

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
    public EstadoSesion getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoSesion estadoAnterior) { this.estadoAnterior = estadoAnterior; }
    public EstadoSesion getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(EstadoSesion estadoNuevo) { this.estadoNuevo = estadoNuevo; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
