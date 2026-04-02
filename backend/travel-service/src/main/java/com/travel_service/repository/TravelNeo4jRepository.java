package com.travel_service.repository;

import com.travel_service.model.TravelNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface TravelNeo4jRepository extends Neo4jRepository<TravelNode, Long> {
}