package com.smartwardrobe.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackDTO {

    @NotNull(message = "反馈类型不能为空")
    private Integer type;  // 1喜欢, 2不喜欢

    private String reason;
}
