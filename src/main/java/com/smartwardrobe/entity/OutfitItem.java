package com.smartwardrobe.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("outfit_item")
public class OutfitItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("outfit_id")
    private Long outfitId;

    @TableField("item_id")
    private Long itemId;

    private Integer position;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private Item item;

    @TableField(exist = false)
    private String positionName;

    public String getPositionName() {
        return switch (position) {
            case 1 -> "上装";
            case 2 -> "下装";
            case 3 -> "鞋履";
            case 4 -> "配饰";
            default -> "其他";
        };
    }
}
