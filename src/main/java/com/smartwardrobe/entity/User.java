package com.smartwardrobe.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String openid;

    @TableField("union_id")
    private String unionId;

    private String nickname;

    @TableField("avatar_url")
    private String avatarUrl;

    private Integer gender;

    private String phone;

    private Integer status;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
