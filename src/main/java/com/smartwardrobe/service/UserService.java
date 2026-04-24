package com.smartwardrobe.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartwardrobe.entity.Item;
import com.smartwardrobe.entity.Outfit;
import com.smartwardrobe.entity.User;
import com.smartwardrobe.mapper.ItemMapper;
import com.smartwardrobe.mapper.OutfitMapper;
import com.smartwardrobe.mapper.UserMapper;
import com.smartwardrobe.mapper.WearRecordMapper;
import com.smartwardrobe.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final OutfitMapper outfitMapper;
    private final WearRecordMapper wearRecordMapper;

    public UserInfoVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setGender(user.getGender());
        vo.setPhone(user.getPhone());
        vo.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);

        // 统计数据
        UserInfoVO.UserStatsVO stats = new UserInfoVO.UserStatsVO();

        // 单品数量
        Long itemCount = itemMapper.countByUserId(userId);
        stats.setItemCount(itemCount);

        // 搭配数量
        Long outfitCount = outfitMapper.selectCount(
                new LambdaQueryWrapper<Outfit>()
                        .eq(Outfit::getUserId, userId)
                        .eq(Outfit::getDeleted, 0)
        );
        stats.setOutfitCount(outfitCount);

        // 沉睡单品数量
        Long sleepingCount = (long) itemMapper.findSleepingItems(userId, 90).size();
        stats.setSleepingCount(sleepingCount);

        // 使用天数
        Long usedDays = wearRecordMapper.countWearDays(userId,
                LocalDate.now().minusMonths(1).withDayOfMonth(1),
                LocalDate.now());
        stats.setUsedDays(usedDays);

        vo.setStats(stats);

        return vo;
    }

    public void updateUserInfo(Long userId, String nickname, String avatarUrl, Integer gender) {
        User user = new User();
        user.setId(userId);
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        if (gender != null) {
            user.setGender(gender);
        }
        userMapper.updateById(user);
    }
}
