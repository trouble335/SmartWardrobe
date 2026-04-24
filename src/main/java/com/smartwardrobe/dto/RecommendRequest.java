package com.smartwardrobe.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecommendRequest {

    private BigDecimal temperature;
    private String location;
    private String occasion;
    private String style;
    private Long itemId;
    private Integer count;
}
