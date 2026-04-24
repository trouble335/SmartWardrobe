package com.smartwardrobe.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_import")
public class OrderImport {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("image_url")
    private String imageUrl;

    @TableField("ocr_result")
    private String ocrResult;

    private Integer status;

    @TableField("item_id")
    private Long itemId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private OrderRecognizedInfo recognizedInfo;

    @Data
    public static class OrderRecognizedInfo {
        private String name;
        private java.math.BigDecimal price;
        private String platform;
        private String purchaseDate;
        private String imageUrl;
    }
}
