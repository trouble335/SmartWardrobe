package com.smartwardrobe.vo;

import lombok.Data;

import java.util.List;

@Data
public class LocationVO {

    private Long id;
    private String name;
    private Long parentId;
    private List<LocationVO> children;
}
