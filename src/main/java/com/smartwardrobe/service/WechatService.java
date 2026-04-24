package com.smartwardrobe.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.smartwardrobe.config.JwtConfig;
import com.smartwardrobe.dto.WechatLoginRequest;
import com.smartwardrobe.entity.User;
import com.smartwardrobe.exception.BusinessException;
import com.smartwardrobe.mapper.UserMapper;
import com.smartwardrobe.vo.LoginResultVO;
import com.smartwardrobe.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatService {

    private final UserMapper userMapper;
    private final JwtConfig jwtConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${wechat.appid}")
    private String appId;

    @Value("${wechat.secret}")
    private String appSecret;

    private static final String JSCODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    @Transactional
    public LoginResultVO login(WechatLoginRequest request) {
        // 1. 调用微信接口获取 openid
        WechatSession session = code2Session(request.getCode());

        // 2. 查询用户
        User user = userMapper.findByOpenid(session.getOpenid());

        boolean isNewUser = false;
        if (user == null) {
            // 新用户注册
            user = new User();
            user.setOpenid(session.getOpenid());
            user.setUnionId(session.getUnionid());
            user.setNickname(request.getNickname() != null ? request.getNickname() : "时尚达人");
            user.setAvatarUrl(request.getAvatarUrl());
            user.setGender(request.getGender() != null ? request.getGender() : 0);
            user.setStatus(1);
            user.setLastLoginAt(LocalDateTime.now());
            userMapper.insert(user);
            isNewUser = true;
        } else {
            // 更新用户信息
            user.setLastLoginAt(LocalDateTime.now());
            if (request.getNickname() != null) {
                user.setNickname(request.getNickname());
            }
            if (request.getAvatarUrl() != null) {
                user.setAvatarUrl(request.getAvatarUrl());
            }
            userMapper.updateById(user);
        }

        // 3. 生成 token
        String token = jwtConfig.generateToken(user.getId(), user.getOpenid());

        // 4. 缓存用户信息
        String tokenKey = "token:" + token;
        redisTemplate.opsForValue().set(tokenKey, user.getId(), jwtConfig.getExpirationTime(), TimeUnit.SECONDS);

        // 5. 构建返回结果
        LoginResultVO result = new LoginResultVO();
        result.setToken(token);
        result.setExpireIn(jwtConfig.getExpirationTime());

        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(user.getId());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setGender(user.getGender());

        UserInfoVO.UserStatsVO stats = new UserInfoVO.UserStatsVO();
        stats.setItemCount(0L);
        stats.setOutfitCount(0L);
        stats.setSleepingCount(0L);
        stats.setUsedDays(0L);
        userInfo.setStats(stats);

        result.setUserInfo(userInfo);

        log.info("用户登录成功: userId={}, openid={}, isNewUser={}", user.getId(), session.getOpenid(), isNewUser);

        return result;
    }

    public WechatSession code2Session(String code) {
        String url = String.format(JSCODE2SESSION_URL, appId, appSecret, code);

        try {
            String response = restTemplate.getForObject(url, String.class);
            log.debug("微信登录响应: {}", response);

            JSONObject json = JSON.parseObject(response);

            if (json.containsKey("errcode") && json.getIntValue("errcode") != 0) {
                log.error("微信登录失败: {}", json.getString("errmsg"));
                throw new BusinessException(40001, "微信登录失败: " + json.getString("errmsg"));
            }

            WechatSession session = new WechatSession();
            session.setOpenid(json.getString("openid"));
            session.setSessionKey(json.getString("session_key"));
            session.setUnionid(json.getString("unionid"));
            return session;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信接口异常", e);
            throw new BusinessException(40001, "微信接口调用失败");
        }
    }

    @lombok.Data
    public static class WechatSession {
        private String openid;
        private String sessionKey;
        private String unionid;
    }
}
