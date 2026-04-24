package com.smartwardrobe.dto;

import lombok.Data;

import java.util.List;

@Data
public class ItemQueryDTO {

    private Long categoryId;
    private List<String> styles;
    private List<String> seasons;
    private List<String> occasions;
    private List<String> colors;
    private Integer status;
    private String keyword;
    private String sortBy;
    private String sortOrder;
    private Integer page = 1;
    private Integer size = 20;
}
