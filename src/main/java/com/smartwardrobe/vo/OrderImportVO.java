package com.smartwardrobe.vo;

import com.smartwardrobe.entity.OrderImport;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderImportVO {

    private Long importId;
    private Integer status;
    private OrderImport.OrderRecognizedInfo recognizedInfo;
}
