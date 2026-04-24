package com.smartwardrobe.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WearRecordDTO {

    @NotNull(message = "穿着日期不能为空")
    private LocalDate wearDate;

    private String occasion;

    private String note;
}
