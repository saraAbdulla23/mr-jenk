package com.user_service.dto;

public class ProductCount {

    private String key;
    private Integer value;

    public ProductCount(String key, Integer value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }
}