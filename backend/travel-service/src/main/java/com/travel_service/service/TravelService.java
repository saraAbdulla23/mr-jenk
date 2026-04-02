package com.travel_service.service;

import com.travel_service.dto.TravelDTO;
import com.travel_service.model.TravelEntity;
import com.travel_service.model.TravelNode;
import com.travel_service.repository.TravelJpaRepository;
import com.travel_service.repository.TravelNeo4jRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TravelService {

    private final TravelJpaRepository jpaRepo;
    private final TravelNeo4jRepository neoRepo;

    public TravelService(TravelJpaRepository jpaRepo,
                         TravelNeo4jRepository neoRepo) {
        this.jpaRepo = jpaRepo;
        this.neoRepo = neoRepo;
    }

    // ================= CREATE =================
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TravelDTO createTravel(TravelDTO dto, String userId) {

        // Save in PostgreSQL
        TravelEntity entity = new TravelEntity();
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setDuration(dto.getDuration());
        entity.setAccommodation(dto.getAccommodation());
        entity.setTransportation(dto.getTransportation());
        entity.setUserId(userId);
        entity = jpaRepo.save(entity);

        // Save in Neo4j
        TravelNode node = new TravelNode();
        node.setDestinations(dto.getDestinations());
        node.setActivities(dto.getActivities());
        node.setTravelId(entity.getId());
        neoRepo.save(node);

        dto.setId(entity.getId());
        return dto;
    }

    // ================= READ =================
    public List<TravelDTO> getAllTravels() {
        return jpaRepo.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public TravelDTO getTravelById(Long id) {
        return jpaRepo.findById(id).map(this::mapToDTO).orElse(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<TravelDTO> getTravelsByUserId(String userId) {
        return jpaRepo.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ================= UPDATE =================
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TravelDTO updateTravel(String userId, Long id, TravelDTO dto) {

        TravelEntity entity = jpaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (!entity.getUserId().equals(userId))
            throw new SecurityException("Not allowed");

        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setDuration(dto.getDuration());
        entity.setAccommodation(dto.getAccommodation());
        entity.setTransportation(dto.getTransportation());
        jpaRepo.save(entity);

        // update Neo4j
        TravelNode node = neoRepo.findAll().stream()
                .filter(n -> n.getTravelId().equals(id))
                .findFirst()
                .orElseThrow();
        node.setDestinations(dto.getDestinations());
        node.setActivities(dto.getActivities());
        neoRepo.save(node);

        return dto;
    }

    // ================= DELETE =================
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteTravel(String userId, Long id) {

        TravelEntity entity = jpaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (!entity.getUserId().equals(userId))
            throw new SecurityException("Not allowed");

        jpaRepo.deleteById(id);

        neoRepo.findAll().stream()
                .filter(n -> n.getTravelId().equals(id))
                .forEach(neoRepo::delete);
    }

    // ================= MAPPER =================
    private TravelDTO mapToDTO(TravelEntity entity) {

        TravelNode node = neoRepo.findAll().stream()
                .filter(n -> n.getTravelId().equals(entity.getId()))
                .findFirst()
                .orElse(null);

        TravelDTO dto = new TravelDTO();
        dto.setId(entity.getId());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setDuration(entity.getDuration());
        dto.setAccommodation(entity.getAccommodation());
        dto.setTransportation(entity.getTransportation());

        if (node != null) {
            dto.setDestinations(node.getDestinations());
            dto.setActivities(node.getActivities());
        }

        return dto;
    }
}