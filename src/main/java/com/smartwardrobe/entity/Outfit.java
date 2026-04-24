package com.smartwardrobe.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("outfit")
public class Outfit {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String name;

    private String description;

    private String style;

    private String occasion;

    private String season;

    @TableField("image_url")
    private String imageUrl;

    @TableField("is_recommended")
    private Integer isRecommended;

    @TableField("wear_count")
    private Integer wearCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(exist = false)
    private List<OutfitItem> items;
}
