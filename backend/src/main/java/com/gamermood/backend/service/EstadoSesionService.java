package com.gamermood.backend.service;

import com.gamermood.backend.entity.EstadoSesion;
import com.gamermood.backend.entity.GameSession;
import com.gamermood.backend.entity.TransicionEstado;
import com.gamermood.backend.exception.RecursoNoEncontradoException;
import com.gamermood.backend.repository.SessionRepository;
import com.gamermood.backend.repository.TransicionEstadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstadoSesionService {

    private final TransicionEstadoRepository transicionRepository;
    private final SessionRepository sessionRepository;

    public EstadoSesionService(TransicionEstadoRepository transicionRepository,
                               SessionRepository sessionRepository) {
        this.transicionRepository = transicionRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public TransicionEstado avanzar(Long sesionId, EstadoSesion nuevoEstado, String motivo) {
        GameSession sesion = sessionRepository.findById(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión no encontrada"));

        EstadoSesion estadoActual = obtenerEstadoActual(sesionId);

        TransicionEstado transicion = new TransicionEstado();
        transicion.setSesion(sesion);
        transicion.setEstadoAnterior(estadoActual);
        transicion.setEstadoNuevo(nuevoEstado);
        transicion.setMotivo(motivo);

        return transicionRepository.save(transicion);
    }

    public EstadoSesion obtenerEstadoActual(Long sesionId) {
        return transicionRepository.findTopBySesionIdOrderByCreatedAtDesc(sesionId)
                .map(TransicionEstado::getEstadoNuevo)
                .orElse(EstadoSesion.SESSION_CREATED);
    }

    public List<TransicionEstado> obtenerHistorial(Long sesionId) {
        return transicionRepository.findBySesionIdOrderByCreatedAtAsc(sesionId);
    }
}
