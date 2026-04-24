package com.smartwardrobe.vo;

import lombok.Data;

import java.util.List;

@Data
public class CategoryVO {

    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private String icon;
    private List<CategoryVO> children;
}
