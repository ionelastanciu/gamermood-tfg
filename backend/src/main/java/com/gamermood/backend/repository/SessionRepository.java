package com.gamermood.backend.repository;

import com.gamermood.backend.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<GameSession, Long> {

    List<GameSession> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);

    Optional<GameSession> findByIdAndUsuarioId(Long id, Long usuarioId);
}
