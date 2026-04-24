package com.smartwardrobe.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WearRecordVO {

    private Long id;
    private LocalDate wearDate;
    private BigDecimal weatherTemp;
    private String weatherDesc;
    private String occasion;
    private String note;
    private ItemSimpleVO item;

    @Data
    public static class ItemSimpleVO {
        private Long id;
        private String name;
        private String imageUrl;
    }
}
