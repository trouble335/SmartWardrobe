package com.smartwardrobe.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class WardrobeReportVO {

    private String month;
    private Integer wearRate;
    private Integer totalWearDays;

    private List<CategoryDistribution> categoryDistribution;
    private List<ColorDistribution> colorDistribution;
    private List<TopWornItem> topWornItems;
    private List<SleepingItem> sleepingItems;

    @Data
    public static class CategoryDistribution {
        private String name;
        private Long count;
        private Integer percentage;
    }

    @Data
    public static class ColorDistribution {
        private String name;
        private String color;
        private Long count;
        private Integer percentage;
    }

    @Data
    public static class TopWornItem {
        private Long id;
        private String name;
        private String imageUrl;
        private Integer wearCount;
    }

    @Data
    public static class SleepingItem {
        private Long id;
        private String name;
        private String imageUrl;
        private Integer sleepingDays;
    }
}
