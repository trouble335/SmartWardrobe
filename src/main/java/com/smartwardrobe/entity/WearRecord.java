package com.smartwardrobe.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("wear_record")
public class WearRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("item_id")
    private Long itemId;

    @TableField("outfit_id")
    private Long outfitId;

    @TableField("wear_date")
    private LocalDate wearDate;

    @TableField("weather_temp")
    private BigDecimal weatherTemp;

    @TableField("weather_desc")
    private String weatherDesc;

    private String occasion;

    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private Item item;

    @TableField(exist = false)
    private Outfit outfit;
}
