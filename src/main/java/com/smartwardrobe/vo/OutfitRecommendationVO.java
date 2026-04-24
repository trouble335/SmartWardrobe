package com.smartwardrobe.vo;

import lombok.Data;

import java.util.List;

@Data
public class OutfitRecommendationVO {

    private WeatherVO weather;
    private List<RecommendationItem> recommendations;
    private List<AlternativeItem> alternatives;

    @Data
    public static class RecommendationItem {
        private String id;
        private String style;
        private String occasion;
        private String description;
        private List<OutfitItemVO> items;
        private String matchReason;
        private Integer score;
    }

    @Data
    public static class OutfitItemVO {
        private Integer position;
        private String positionName;
        private ItemSimpleVO item;
    }

    @Data
    public static class ItemSimpleVO {
        private Long id;
        private String name;
        private String imageUrl;
        private String locationName;
    }

    @Data
    public static class AlternativeItem {
        private String id;
        private String style;
        private String previewUrl;
    }
}
