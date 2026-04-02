package com.travel_service.repository;

import com.travel_service.model.TravelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelJpaRepository extends JpaRepository<TravelEntity, Long> {
    List<TravelEntity> findByUserId(String userId);
}