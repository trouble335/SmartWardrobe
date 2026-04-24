package com.smartwardrobe.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WeatherVO {

    private String city;
    private BigDecimal temp;
    private String description;
    private Integer humidity;
    private String windSpeed;
    private String suggestion;
}
