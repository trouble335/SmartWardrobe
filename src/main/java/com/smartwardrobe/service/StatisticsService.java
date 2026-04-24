package com.smartwardrobe.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartwardrobe.entity.Item;
import com.smartwardrobe.entity.WearRecord;
import com.smartwardrobe.mapper.CategoryMapper;
import com.smartwardrobe.mapper.ItemMapper;
import com.smartwardrobe.mapper.WearRecordMapper;
import com.smartwardrobe.vo.WardrobeReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ItemMapper itemMapper;
    private final CategoryMapper categoryMapper;
    private final WearRecordMapper wearRecordMapper;

    private static final int SLEEPING_THRESHOLD_DAYS = 90;

    public WardrobeReportVO getReport(Long userId, String month) {
        // 解析月份
        YearMonth yearMonth;
        if (month == null || month.isEmpty()) {
            yearMonth = YearMonth.now();
        } else {
            yearMonth = YearMonth.parse(month);
        }
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        WardrobeReportVO report = new WardrobeReportVO();
        report.setMonth(yearMonth.toString());

        // 计算穿着天数和穿着率
        Long wearDays = wearRecordMapper.countWearDays(userId, startDate, endDate);
        int daysInMonth = yearMonth.lengthOfMonth();
        report.setTotalWearDays(wearDays != null ? wearDays.intValue() : 0);
        report.setWearRate((int) ((wearDays != null ? wearDays : 0) * 100 / daysInMonth));

        // 分类分布
        report.setCategoryDistribution(getCategoryDistribution(userId));

        // 颜色分布
        report.setColorDistribution(getColorDistribution(userId));

        // 最常穿单品
        report.setTopWornItems(getTopWornItems(userId, 10));

        // 沉睡单品
        report.setSleepingItems(getSleepingItems(userId));

        return report;
    }

    private List<WardrobeReportVO.CategoryDistribution> getCategoryDistribution(Long userId) {
        // 获取所有单品
        List<Item> items = itemMapper.selectList(
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getUserId, userId)
                        .eq(Item::getDeleted, 0)
                        .eq(Item::getStatus, 1)
        );

        // 按分类分组统计
        Map<Long, Long> categoryCount = items.stream()
                .collect(Collectors.groupingBy(Item::getCategoryId, Collectors.counting()));

        long total = items.size();
        if (total == 0) {
            return new ArrayList<>();
        }

        // 分类名称映射
        Map<Long, String> categoryNames = new HashMap<>();
        categoryNames.put(1L, "上装");
        categoryNames.put(2L, "下装");
        categoryNames.put(3L, "鞋履");
        categoryNames.put(4L, "配饰");

        return categoryCount.entrySet().stream()
                .map(entry -> {
                    WardrobeReportVO.CategoryDistribution dist = new WardrobeReportVO.CategoryDistribution();
                    dist.setName(categoryNames.getOrDefault(entry.getKey(), "其他"));
                    dist.setCount(entry.getValue());
                    dist.setPercentage((int) (entry.getValue() * 100 / total));
                    return dist;
                })
                .sorted((a, b) -> b.getCount().compareTo(a.getCount()))
                .collect(Collectors.toList());
    }

    private List<WardrobeReportVO.ColorDistribution> getColorDistribution(Long userId) {
        // 获取所有单品的颜色
        List<String> colorJsons = itemMapper.findAllColorsByUserId(userId);

        // 统计颜色
        Map<String, Long> colorCount = new HashMap<>();
        for (String colorsJson : colorJsons) {
            if (colorsJson != null && !colorsJson.isEmpty()) {
                List<String> colors = JSON.parseArray(colorsJson, String.class);
                for (String color : colors) {
                    colorCount.merge(color, 1L, Long::sum);
                }
            }
        }

        long total = colorCount.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) {
            return new ArrayList<>();
        }

        // 颜色映射
        Map<String, String> colorHexMap = Map.ofEntries(
                Map.entry("黑色", "#000000"),
                Map.entry("白色", "#FFFFFF"),
                Map.entry("灰色", "#808080"),
                Map.entry("棕色", "#8B4513"),
                Map.entry("蓝色", "#0000FF"),
                Map.entry("绿色", "#008000"),
                Map.entry("红色", "#FF0000"),
                Map.entry("黄色", "#FFFF00"),
                Map.entry("紫色", "#800080"),
                Map.entry("粉色", "#FFC0CB"),
                Map.entry("橙色", "#FFA500")
        );

        return colorCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .map(entry -> {
                    WardrobeReportVO.ColorDistribution dist = new WardrobeReportVO.ColorDistribution();
                    dist.setName(entry.getKey());
                    dist.setColor(colorHexMap.getOrDefault(entry.getKey(), null));
                    dist.setCount(entry.getValue());
                    dist.setPercentage((int) (entry.getValue() * 100 / total));
                    return dist;
                })
                .collect(Collectors.toList());
    }

    private List<WardrobeReportVO.TopWornItem> getTopWornItems(Long userId, int limit) {
        List<Item> items = itemMapper.selectList(
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getUserId, userId)
                        .eq(Item::getDeleted, 0)
                        .eq(Item::getStatus, 1)
                        .gt(Item::getWearCount, 0)
                        .orderByDesc(Item::getWearCount)
                        .last("LIMIT " + limit)
        );

        return items.stream().map(item -> {
            WardrobeReportVO.TopWornItem vo = new WardrobeReportVO.TopWornItem();
            vo.setId(item.getId());
            vo.setName(item.getName());
            vo.setImageUrl(item.getImageUrl());
            vo.setWearCount(item.getWearCount());
            return vo;
        }).collect(Collectors.toList());
    }

    private List<WardrobeReportVO.SleepingItem> getSleepingItems(Long userId) {
        List<Item> items = itemMapper.findSleepingItems(userId, SLEEPING_THRESHOLD_DAYS);

        return items.stream().map(item -> {
            WardrobeReportVO.SleepingItem vo = new WardrobeReportVO.SleepingItem();
            vo.setId(item.getId());
            vo.setName(item.getName());
            vo.setImageUrl(item.getImageUrl());
            int sleepingDays = item.getLastWorn() != null
                    ? (int) ChronoUnit.DAYS.between(item.getLastWorn(), LocalDate.now())
                    : Integer.MAX_VALUE;
            vo.setSleepingDays(sleepingDays);
            return vo;
        }).collect(Collectors.toList());
    }

    public List<WardrobeReportVO.SleepingItem> getSleepingItemsList(Long userId, int days, int page, int size) {
        int offset = (page - 1) * size;
        List<Item> items = itemMapper.findSleepingItems(userId, days);

        return items.stream()
                .skip(offset)
                .limit(size)
                .map(item -> {
                    WardrobeReportVO.SleepingItem vo = new WardrobeReportVO.SleepingItem();
                    vo.setId(item.getId());
                    vo.setName(item.getName());
                    vo.setImageUrl(item.getImageUrl());
                    int sleepingDays = item.getLastWorn() != null
                            ? (int) ChronoUnit.DAYS.between(item.getLastWorn(), LocalDate.now())
                            : Integer.MAX_VALUE;
                    vo.setSleepingDays(sleepingDays);
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
