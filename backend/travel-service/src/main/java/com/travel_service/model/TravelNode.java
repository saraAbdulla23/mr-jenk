package com.travel_service.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;

@Node("Travel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelNode {

    @Id
    @GeneratedValue
    private Long id;

    @Property("destinations")
    private List<String> destinations;

    @Property("activities")
    private List<String> activities;

    @Property("travelId") // link to Postgres
    private Long travelId;
}