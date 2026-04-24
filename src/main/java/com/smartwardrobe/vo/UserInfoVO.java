package com.smartwardrobe.vo;

import lombok.Data;

@Data
public class UserInfoVO {

    private Long id;
    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private String phone;
    private UserStatsVO stats;
    private String createdAt;

    @Data
    public static class UserStatsVO {
        private Long itemCount;
        private Long outfitCount;
        private Long sleepingCount;
        private Long usedDays;
    }
}
