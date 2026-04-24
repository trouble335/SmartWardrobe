package com.smartwardrobe.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("item")
public class Item {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String name;

    @TableField("category_id")
    private Long categoryId;

    @TableField("sub_category_id")
    private Long subCategoryId;

    private String colors;

    private String size;

    private String material;

    private String seasons;

    private String styles;

    private String occasions;

    @TableField("image_url")
    private String imageUrl;

    @TableField("original_image_url")
    private String originalImageUrl;

    private Integer status;

    @TableField("wear_count")
    private Integer wearCount;

    @TableField("last_worn")
    private LocalDate lastWorn;

    @TableField("location_id")
    private Long locationId;

    private BigDecimal price;

    private String platform;

    @TableField("purchase_date")
    private LocalDate purchaseDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    // 非数据库字段
    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private String subCategoryName;

    @TableField(exist = false)
    private String locationName;

    @TableField(exist = false)
    private Integer sleepingDays;

    @TableField(exist = false)
    private BigDecimal costPerWear;

    @TableField(exist = false)
    private List<Outfit> outfits;

    public List<String> getColorList() {
        if (colors == null || colors.isEmpty()) {
            return List.of();
        }
        return com.alibaba.fastjson2.JSON.parseArray(colors, String.class);
    }

    public List<String> getSeasonList() {
        if (seasons == null || seasons.isEmpty()) {
            return List.of();
        }
        return com.alibaba.fastjson2.JSON.parseArray(seasons, String.class);
    }

    public List<String> getStyleList() {
        if (styles == null || styles.isEmpty()) {
            return List.of();
        }
        return com.alibaba.fastjson2.JSON.parseArray(styles, String.class);
    }

    public List<String> getOccasionList() {
        if (occasions == null || occasions.isEmpty()) {
            return List.of();
        }
        return com.alibaba.fastjson2.JSON.parseArray(occasions, String.class);
    }
}
