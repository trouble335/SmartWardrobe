package com.smartwardrobe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TagDTO {

    @NotBlank(message = "标签类型不能为空")
    @Pattern(regexp = "style|season|occasion|color", message = "标签类型只能是 style、season、occasion、color")
    private String type;

    @NotBlank(message = "标签编码不能为空")
    private String code;

    @NotBlank(message = "标签名称不能为空")
    private String name;

    private String colorHex;

    private Integer sortOrder;
}
