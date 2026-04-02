package com.travel_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class TravelDTO {

    private Long id;

    private List<String> destinations;
    private List<String> activities;

    private String startDate;
    private String endDate;
    private int duration;

    private String accommodation;
    private String transportation;
}