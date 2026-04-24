package com.smartwardrobe.controller;

import com.smartwardrobe.dto.Result;
import com.smartwardrobe.dto.WechatLoginRequest;
import com.smartwardrobe.service.WechatService;
import com.smartwardrobe.vo.LoginResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final WechatService wechatService;

    @PostMapping("/wechat/login")
    public Result<LoginResultVO> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        LoginResultVO result = wechatService.login(request);
        return Result.success(result);
    }
}
