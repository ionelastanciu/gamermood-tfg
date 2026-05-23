package com.gamermood.backend.repository;

import com.gamermood.backend.entity.FeedbackRecomendacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackRecomendacion, Long> {

    boolean existsByRecomendacionId(Long recomendacionId);
}
