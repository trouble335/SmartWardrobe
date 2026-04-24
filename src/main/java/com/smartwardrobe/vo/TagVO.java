package com.smartwardrobe.vo;

import lombok.Data;

@Data
public class TagVO {

    private Long id;
    private String type;
    private String code;
    private String name;
    private String colorHex;
    private Integer sortOrder;
}
