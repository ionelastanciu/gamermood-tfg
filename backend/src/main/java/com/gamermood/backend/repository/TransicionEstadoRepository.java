package com.gamermood.backend.repository;

import com.gamermood.backend.entity.TransicionEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransicionEstadoRepository extends JpaRepository<TransicionEstado, Long> {

    List<TransicionEstado> findBySesionIdOrderByCreatedAtAsc(Long sesionId);

    Optional<TransicionEstado> findTopBySesionIdOrderByCreatedAtDesc(Long sesionId);
}
