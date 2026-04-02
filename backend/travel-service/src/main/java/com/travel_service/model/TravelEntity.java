package com.travel_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "travels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String startDate;
    private String endDate;
    private int duration;

    private String accommodation;
    private String transportation;

    private String userId;
}