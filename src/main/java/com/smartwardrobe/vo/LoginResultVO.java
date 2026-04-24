package com.smartwardrobe.vo;

import lombok.Data;

@Data
public class LoginResultVO {

    private String token;
    private Long expireIn;
    private UserInfoVO userInfo;
}
