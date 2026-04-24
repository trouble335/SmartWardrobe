package com.smartwardrobe.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemVO {

    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long subCategoryId;
    private String subCategoryName;
    private List<String> colors;
    private String size;
    private String material;
    private List<String> styles;
    private List<String> seasons;
    private List<String> occasions;
    private String imageUrl;
    private String originalImageUrl;
    private Integer status;
    private Integer wearCount;
    private LocalDate lastWorn;
    private Integer sleepingDays;
    private BigDecimal costPerWear;
    private LocationVO location;
    private PurchaseInfoVO purchaseInfo;
    private List<OutfitVO> outfits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class LocationVO {
        private Long id;
        private String name;
    }

    @Data
    public static class PurchaseInfoVO {
        private BigDecimal price;
        private String platform;
        private LocalDate date;
    }

    @Data
    public static class OutfitVO {
        private Long id;
        private String name;
        private String imageUrl;
        private Integer wearCount;
    }
}
