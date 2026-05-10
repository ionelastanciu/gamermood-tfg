package com.gamermood.backend.repository;

import com.gamermood.backend.entity.Recomendacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecomendacionRepository extends JpaRepository<Recomendacion, Long> {

    Optional<Recomendacion> findBySesionId(Long sesionId);
}
