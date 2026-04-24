package com.smartwardrobe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ItemCreateDTO {

    @NotBlank(message = "单品名称不能为空")
    private String name;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private Long subCategoryId;

    private List<String> colors;

    private String size;

    private String material;

    private List<String> seasons;

    private List<String> styles;

    private List<String> occasions;

    @NotBlank(message = "图片不能为空")
    private String imageUrl;

    private String originalImageUrl;

    private Long locationId;

    private BigDecimal price;

    private String platform;

    private LocalDate purchaseDate;
}
