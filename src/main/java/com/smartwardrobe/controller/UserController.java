package com.smartwardrobe.controller;

import com.smartwardrobe.dto.Result;
import com.smartwardrobe.entity.User;
import com.smartwardrobe.service.UserService;
import com.smartwardrobe.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserInfoVO userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    @PutMapping("/info")
    public Result<Void> updateUserInfo(HttpServletRequest request,
                                       @RequestParam(required = false) String nickname,
                                       @RequestParam(required = false) String avatarUrl,
                                       @RequestParam(required = false) Integer gender) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateUserInfo(userId, nickname, avatarUrl, gender);
        return Result.success();
    }
}
