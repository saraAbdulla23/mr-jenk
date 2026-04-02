package com.travel_service.controller;

import com.travel_service.dto.TravelDTO;
import com.travel_service.service.TravelService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/travels")
public class TravelController {

    private final TravelService travelService;

    public TravelController(TravelService travelService) {
        this.travelService = travelService;
    }

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<?> createTravel(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TravelDTO travelRequest) {
        try {
            String userId = jwt.getClaimAsString("userId");
            TravelDTO created = travelService.createTravel(travelRequest, userId);
            return ResponseEntity.ok(created);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================= READ =================
    @GetMapping
    public ResponseEntity<List<TravelDTO>> getAllTravels() {
        return ResponseEntity.ok(travelService.getAllTravels());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTravelById(@PathVariable Long id) {
        TravelDTO travel = travelService.getTravelById(id);

        if (travel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Travel not found"));
        }

        return ResponseEntity.ok(travel);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyTravels(@AuthenticationPrincipal Jwt jwt) {
        try {
            String userId = jwt.getClaimAsString("userId");
            List<TravelDTO> travels = travelService.getTravelsByUserId(userId);
            return ResponseEntity.ok(travels);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTravel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody TravelDTO updated) {

        try {
            String userId = jwt.getClaimAsString("userId");
            TravelDTO travel = travelService.updateTravel(userId, id, updated);
            return ResponseEntity.ok(travel);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTravel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        try {
            String userId = jwt.getClaimAsString("userId");
            travelService.deleteTravel(userId, id);

            return ResponseEntity.ok(Map.of("message", "Travel deleted successfully"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================= SEARCH =================
    @GetMapping("/search")
    public ResponseEntity<List<TravelDTO>> searchByDestination(
            @RequestParam(required = false) String destination) {

        List<TravelDTO> results = travelService.getAllTravels();

        if (destination != null && !destination.isEmpty()) {
            results = results.stream()
                    .filter(t -> t.getDestinations() != null &&
                            t.getDestinations().stream()
                                    .anyMatch(d -> d.toLowerCase().contains(destination.toLowerCase())))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(results);
    }
}