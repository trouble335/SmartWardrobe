package com.smartwardrobe.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartwardrobe.entity.Item;
import com.smartwardrobe.entity.Outfit;
import com.smartwardrobe.entity.OutfitItem;
import com.smartwardrobe.mapper.ItemMapper;
import com.smartwardrobe.mapper.OutfitItemMapper;
import com.smartwardrobe.mapper.OutfitMapper;
import com.smartwardrobe.vo.OutfitRecommendationVO;
import com.smartwardrobe.vo.WeatherVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final ItemMapper itemMapper;
    private final OutfitMapper outfitMapper;
    private final OutfitItemMapper outfitItemMapper;
    private final WeatherService weatherService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.recommend.default-count:3}")
    private int defaultCount;

    @Value("${app.recommend.max-count:5}")
    private int maxCount;

    private static final String RECOMMEND_CACHE_KEY = "recommend:";
    private static final long RECOMMEND_CACHE_TTL = 10;

    // 颜色搭配规则（使用 tag code，与 item 表 JSON 字段保持一致）
    private static final Map<String, List<String>> NEUTRAL_COLORS = Map.of(
            "black", List.of("black", "white", "gray", "brown", "blue", "red", "green", "yellow", "purple", "pink", "orange"),
            "white", List.of("black", "white", "gray", "brown", "blue", "red", "green", "yellow", "purple", "pink", "orange"),
            "gray",  List.of("black", "white", "gray", "brown", "blue", "red", "green", "yellow", "purple", "pink", "orange"),
            "brown", List.of("black", "white", "gray", "brown", "blue", "green", "yellow", "orange")
    );

    private static final Map<String, List<String>> ADJACENT_COLORS = Map.of(
            "blue",   List.of("green", "purple"),
            "green",  List.of("blue", "yellow"),
            "red",    List.of("orange", "purple"),
            "yellow", List.of("green", "orange"),
            "purple", List.of("blue", "red"),
            "orange", List.of("red", "yellow")
    );

    public OutfitRecommendationVO recommend(Long userId, String location, String occasion,
                                            String style, Long itemId, Integer count) {
        count = count == null || count < 1 ? defaultCount : Math.min(count, maxCount);

        // 获取天气
        WeatherVO weather = weatherService.getWeather(location);

        // 获取用户可用单品
        List<Item> items = getAvailableItems(userId);
        if (items.isEmpty()) {
            return buildEmptyResult(weather);
        }

        // 按分类分组
        Map<Long, List<Item>> itemsByCategory = items.stream()
                .collect(Collectors.groupingBy(Item::getCategoryId));

        // 根据温度筛选适用季节
        List<String> suitableSeasons = getSuitableSeasons(weather.getTemp().intValue());

        // 生成推荐
        List<OutfitRecommendationVO.RecommendationItem> recommendations = new ArrayList<>();
        for (int i = 0; i < count && recommendations.size() < count; i++) {
            OutfitRecommendationVO.RecommendationItem recommendation = generateRecommendation(
                    itemsByCategory, suitableSeasons, occasion, style, itemId, weather);
            if (recommendation != null) {
                recommendations.add(recommendation);
            }
        }

        // 生成备选方案
        List<OutfitRecommendationVO.AlternativeItem> alternatives = generateAlternatives(
                recommendations, itemsByCategory, suitableSeasons);

        OutfitRecommendationVO result = new OutfitRecommendationVO();
        result.setWeather(weather);
        result.setRecommendations(recommendations);
        result.setAlternatives(alternatives);

        return result;
    }

    private List<Item> getAvailableItems(Long userId) {
        return itemMapper.selectList(
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getUserId, userId)
                        .eq(Item::getStatus, 1)
                        .eq(Item::getDeleted, 0)
        );
    }

    private List<String> getSuitableSeasons(int temperature) {
        if (temperature < 5) {
            return List.of("winter");
        } else if (temperature < 10) {
            return List.of("winter", "autumn");
        } else if (temperature < 15) {
            return List.of("autumn", "spring");
        } else if (temperature < 20) {
            return List.of("spring", "autumn");
        } else if (temperature < 25) {
            return List.of("spring", "summer", "all");
        } else {
            return List.of("summer", "all");
        }
    }

    private OutfitRecommendationVO.RecommendationItem generateRecommendation(
            Map<Long, List<Item>> itemsByCategory,
            List<String> suitableSeasons,
            String occasion,
            String preferredStyle,
            Long specifiedItemId,
            WeatherVO weather) {

        // 选择上装
        List<Item> tops = filterBySeason(itemsByCategory.get(1L), suitableSeasons);
        if (specifiedItemId != null) {
            Item specifiedItem = tops.stream()
                    .filter(i -> i.getId().equals(specifiedItemId))
                    .findFirst()
                    .orElse(null);
            if (specifiedItem != null) {
                tops = List.of(specifiedItem);
            }
        }

        if (tops == null || tops.isEmpty()) {
            return null;
        }

        Item top = selectItem(tops, occasion, preferredStyle);
        if (top == null) {
            return null;
        }

        // 选择下装（颜色搭配）
        List<Item> bottoms = filterBySeason(itemsByCategory.get(2L), suitableSeasons);
        if (bottoms == null || bottoms.isEmpty()) {
            return null;
        }
        Item bottom = selectMatchingBottom(bottoms, top, occasion, preferredStyle);
        if (bottom == null) {
            bottom = bottoms.get(0);
        }

        // 选择鞋履
        List<Item> shoes = filterBySeason(itemsByCategory.get(3L), suitableSeasons);
        Item shoe = shoes != null && !shoes.isEmpty() ? selectMatchingShoes(shoes, top, bottom) : null;

        // 选择配饰（可选）
        List<Item> accessories = filterBySeason(itemsByCategory.get(4L), suitableSeasons);
        Item accessory = accessories != null && !accessories.isEmpty() ?
                selectAccessory(accessories, top, bottom, shoe) : null;

        // 构建推荐结果
        OutfitRecommendationVO.RecommendationItem recommendation = new OutfitRecommendationVO.RecommendationItem();
        recommendation.setId("rec_" + UUID.randomUUID().toString().substring(0, 8));

        // 推断风格
        String style = inferStyle(top, bottom, shoe);
        recommendation.setStyle(style);
        recommendation.setOccasion(occasion != null ? occasion : "日常");

        // 生成描述
        recommendation.setDescription(generateDescription(style, weather));

        // 构建单品列表
        List<OutfitRecommendationVO.OutfitItemVO> items = new ArrayList<>();

        OutfitRecommendationVO.OutfitItemVO topVO = new OutfitRecommendationVO.OutfitItemVO();
        topVO.setPosition(1);
        topVO.setPositionName("上装");
        topVO.setItem(convertToSimpleVO(top));
        items.add(topVO);

        OutfitRecommendationVO.OutfitItemVO bottomVO = new OutfitRecommendationVO.OutfitItemVO();
        bottomVO.setPosition(2);
        bottomVO.setPositionName("下装");
        bottomVO.setItem(convertToSimpleVO(bottom));
        items.add(bottomVO);

        if (shoe != null) {
            OutfitRecommendationVO.OutfitItemVO shoeVO = new OutfitRecommendationVO.OutfitItemVO();
            shoeVO.setPosition(3);
            shoeVO.setPositionName("鞋履");
            shoeVO.setItem(convertToSimpleVO(shoe));
            items.add(shoeVO);
        }

        if (accessory != null) {
            OutfitRecommendationVO.OutfitItemVO accessoryVO = new OutfitRecommendationVO.OutfitItemVO();
            accessoryVO.setPosition(4);
            accessoryVO.setPositionName("配饰");
            accessoryVO.setItem(convertToSimpleVO(accessory));
            items.add(accessoryVO);
        }

        recommendation.setItems(items);

        // 计算匹配分数和理由
        int score = calculateMatchScore(top, bottom, shoe, accessory, occasion);
        recommendation.setScore(score);
        recommendation.setMatchReason(generateMatchReason(top, bottom, shoe, weather, score));

        return recommendation;
    }

    private List<Item> filterBySeason(List<Item> items, List<String> suitableSeasons) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        return items.stream()
                .filter(item -> {
                    List<String> itemSeasons = item.getSeasonList();
                    if (itemSeasons == null || itemSeasons.isEmpty()) {
                        return true;
                    }
                    return itemSeasons.stream().anyMatch(suitableSeasons::contains);
                })
                .collect(Collectors.toList());
    }

    private Item selectItem(List<Item> items, String occasion, String preferredStyle) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        // 按穿着次数排序，优先推荐穿着较少的
        List<Item> sortedItems = items.stream()
                .sorted((a, b) -> {
                    int wearA = a.getWearCount() != null ? a.getWearCount() : 0;
                    int wearB = b.getWearCount() != null ? b.getWearCount() : 0;
                    return wearA - wearB;
                })
                .collect(Collectors.toList());

        // 如果有场合偏好，优先匹配
        if (occasion != null) {
            for (Item item : sortedItems) {
                List<String> occasions = item.getOccasionList();
                if (occasions != null && occasions.contains(occasion)) {
                    return item;
                }
            }
        }

        // 如果有风格偏好
        if (preferredStyle != null) {
            for (Item item : sortedItems) {
                List<String> styles = item.getStyleList();
                if (styles != null && styles.contains(preferredStyle)) {
                    return item;
                }
            }
        }

        // 返回穿着次数最少的
        return sortedItems.get(0);
    }

    private Item selectMatchingBottom(List<Item> bottoms, Item top, String occasion, String preferredStyle) {
        if (bottoms == null || bottoms.isEmpty()) {
            return null;
        }

        String topColor = getPrimaryColor(top);

        // 计算每个下装的颜色匹配分数
        List<Item> scored = bottoms.stream()
                .map(bottom -> {
                    int score = scoreColorMatch(topColor, getPrimaryColor(bottom));
                    // 加上穿着频率因子
                    int wearCount = bottom.getWearCount() != null ? bottom.getWearCount() : 0;
                    return Map.entry(bottom, score - wearCount * 2);
                })
                .sorted((a, b) -> b.getValue() - a.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return selectItem(scored, occasion, preferredStyle);
    }

    private Item selectMatchingShoes(List<Item> shoes, Item top, Item bottom) {
        if (shoes == null || shoes.isEmpty()) {
            return null;
        }

        String topColor = getPrimaryColor(top);
        String bottomColor = getPrimaryColor(bottom);

        return shoes.stream()
                .map(shoe -> {
                    String shoeColor = getPrimaryColor(shoe);
                    int score = scoreColorMatch(topColor, shoeColor) + scoreColorMatch(bottomColor, shoeColor);
                    int wearCount = shoe.getWearCount() != null ? shoe.getWearCount() : 0;
                    return Map.entry(shoe, score - wearCount * 2);
                })
                .sorted((a, b) -> b.getValue() - a.getValue())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(shoes.get(0));
    }

    private Item selectAccessory(List<Item> accessories, Item top, Item bottom, Item shoe) {
        if (accessories == null || accessories.isEmpty()) {
            return null;
        }

        // 简单选择穿着次数最少的配饰
        return accessories.stream()
                .min((a, b) -> {
                    int wearA = a.getWearCount() != null ? a.getWearCount() : 0;
                    int wearB = b.getWearCount() != null ? b.getWearCount() : 0;
                    return wearA - wearB;
                })
                .orElse(null);
    }

    private String getPrimaryColor(Item item) {
        List<String> colors = item.getColorList();
        return colors != null && !colors.isEmpty() ? colors.get(0) : "black";
    }

    private int scoreColorMatch(String color1, String color2) {
        if (color1 == null || color2 == null) {
            return 70;
        }

        if (color1.equals(color2)) {
            return 90; // 同色系
        }

        // 中性色
        if (NEUTRAL_COLORS.containsKey(color1) && NEUTRAL_COLORS.get(color1).contains(color2)) {
            return 85;
        }
        if (NEUTRAL_COLORS.containsKey(color2) && NEUTRAL_COLORS.get(color2).contains(color1)) {
            return 85;
        }

        // 邻近色
        if (ADJACENT_COLORS.containsKey(color1) && ADJACENT_COLORS.get(color1).contains(color2)) {
            return 80;
        }
        if (ADJACENT_COLORS.containsKey(color2) && ADJACENT_COLORS.get(color2).contains(color1)) {
            return 80;
        }

        // 对比色（需谨慎）
        return 60;
    }

    private String inferStyle(Item top, Item bottom, Item shoe) {
        List<String> topStyles = top.getStyleList();
        List<String> bottomStyles = bottom != null ? bottom.getStyleList() : null;

        // 找到共同风格
        if (topStyles != null && bottomStyles != null) {
            for (String style : topStyles) {
                if (bottomStyles.contains(style)) {
                    return style;
                }
            }
        }

        // 返回上装风格或默认
        return topStyles != null && !topStyles.isEmpty() ? topStyles.get(0) : "casual";
    }

    private String generateDescription(String style, WeatherVO weather) {
        return String.format("%s风格，适合%s天气", style, weather.getSuggestion());
    }

    private String generateMatchReason(Item top, Item bottom, Item shoe, WeatherVO weather, int score) {
        StringBuilder reason = new StringBuilder();

        // 颜色搭配
        String topColor = getPrimaryColor(top);
        String bottomColor = getPrimaryColor(bottom);
        reason.append(String.format("%s%s与%s%s颜色搭配协调", topColor, top.getName(), bottomColor, bottom.getName()));

        // 天气适配
        reason.append("，适合当前").append(weather.getTemp()).append("°C的天气");

        // 分数评价
        if (score >= 80) {
            reason.append("，是一套不错的搭配");
        } else if (score >= 60) {
            reason.append("，可以尝试");
        }

        return reason.toString();
    }

    private int calculateMatchScore(Item top, Item bottom, Item shoe, Item accessory, String occasion) {
        int score = 70;

        // 颜色搭配分数
        score += scoreColorMatch(getPrimaryColor(top), getPrimaryColor(bottom)) - 70;
        if (shoe != null) {
            score = (score + scoreColorMatch(getPrimaryColor(top), getPrimaryColor(shoe))) / 2 + 5;
        }

        // 场合匹配
        if (occasion != null) {
            boolean occasionMatch = (top.getOccasionList() != null && top.getOccasionList().contains(occasion))
                    || (bottom != null && bottom.getOccasionList() != null && bottom.getOccasionList().contains(occasion));
            if (occasionMatch) {
                score += 10;
            }
        }

        return Math.min(100, Math.max(0, score));
    }

    private OutfitRecommendationVO.ItemSimpleVO convertToSimpleVO(Item item) {
        OutfitRecommendationVO.ItemSimpleVO vo = new OutfitRecommendationVO.ItemSimpleVO();
        vo.setId(item.getId());
        vo.setName(item.getName());
        vo.setImageUrl(item.getImageUrl());
        // 位置名称需要查询
        return vo;
    }

    private List<OutfitRecommendationVO.AlternativeItem> generateAlternatives(
            List<OutfitRecommendationVO.RecommendationItem> recommendations,
            Map<Long, List<Item>> itemsByCategory,
            List<String> suitableSeasons) {

        List<OutfitRecommendationVO.AlternativeItem> alternatives = new ArrayList<>();
        String[] styles = {"简约休闲", "轻熟商务", "运动活力", "复古文艺"};

        int index = 0;
        for (String style : styles) {
            if (alternatives.size() >= 2) break;

            // 检查是否已有该风格的推荐
            boolean hasStyle = recommendations.stream()
                    .anyMatch(r -> style.contains(r.getStyle()) || r.getStyle().contains(style.substring(0, 2)));
            if (!hasStyle) {
                OutfitRecommendationVO.AlternativeItem alt = new OutfitRecommendationVO.AlternativeItem();
                alt.setId("alt_" + UUID.randomUUID().toString().substring(0, 8));
                alt.setStyle(style);
                alt.setPreviewUrl(null);
                alternatives.add(alt);
            }
        }

        return alternatives;
    }

    private OutfitRecommendationVO buildEmptyResult(WeatherVO weather) {
        OutfitRecommendationVO result = new OutfitRecommendationVO();
        result.setWeather(weather);
        result.setRecommendations(new ArrayList<>());
        result.setAlternatives(new ArrayList<>());
        return result;
    }

    public void recordFeedback(Long userId, Long outfitId, Integer type, String reason) {
        // 保存反馈记录（可以用于后续优化推荐算法）
        log.info("记录推荐反馈: userId={}, outfitId={}, type={}, reason={}", userId, outfitId, type, reason);
    }

    public void recordWear(Long userId, Long outfitId, LocalDate wearDate, String note) {
        // 记录穿搭穿着
        log.info("记录穿搭穿着: userId={}, outfitId={}, date={}, note={}", userId, outfitId, wearDate, note);

        // 更新搭配中所有单品的穿着次数
        List<OutfitItem> outfitItems = outfitItemMapper.findByOutfitId(outfitId);
        for (OutfitItem outfitItem : outfitItems) {
            itemMapper.incrementWearCount(outfitItem.getItemId(), wearDate);
        }
    }
}
