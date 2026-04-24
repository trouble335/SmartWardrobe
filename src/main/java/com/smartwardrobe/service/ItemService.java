package com.smartwardrobe.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartwardrobe.dto.ItemCreateDTO;
import com.smartwardrobe.dto.ItemQueryDTO;
import com.smartwardrobe.dto.PageResult;
import com.smartwardrobe.dto.WearRecordDTO;
import com.smartwardrobe.entity.Item;
import com.smartwardrobe.entity.Outfit;
import com.smartwardrobe.entity.WearRecord;
import com.smartwardrobe.exception.BusinessException;
import com.smartwardrobe.mapper.*;
import com.smartwardrobe.vo.ItemVO;
import com.smartwardrobe.vo.WearRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemMapper itemMapper;
    private final CategoryMapper categoryMapper;
    private final LocationMapper locationMapper;
    private final OutfitMapper outfitMapper;
    private final OutfitItemMapper outfitItemMapper;
    private final WearRecordMapper wearRecordMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ITEM_DETAIL_KEY = "item:";
    private static final int SLEEPING_THRESHOLD_DAYS = 90;

    public PageResult<ItemVO> getItems(Long userId, ItemQueryDTO query) {
        Page<Item> page = new Page<>(query.getPage(), query.getSize());
        List<String> styles = query.getStyles() != null && !query.getStyles().isEmpty() ? query.getStyles() : null;
        List<String> seasons = query.getSeasons() != null && !query.getSeasons().isEmpty() ? query.getSeasons() : null;
        List<String> occasions = query.getOccasions() != null && !query.getOccasions().isEmpty() ? query.getOccasions() : null;
        List<String> colors = query.getColors() != null && !query.getColors().isEmpty() ? query.getColors() : null;

        List<Item> items = itemMapper.findListWithFilters(page, userId,
                query.getCategoryId(),
                styles, seasons, occasions, colors,
                query.getStatus(),
                query.getKeyword(),
                query.getSortBy(),
                query.getSortOrder());

        List<ItemVO> voList = items.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public ItemVO getItemDetail(Long userId, Long id) {
        String cacheKey = ITEM_DETAIL_KEY + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (ItemVO) cached;
        }

        Item item = itemMapper.findDetailById(id);
        if (item == null) {
            return null;
        }

        if (!item.getUserId().equals(userId)) {
            return null;
        }

        ItemVO vo = convertToVO(item);

        // 计算性价比
        if (item.getPrice() != null && item.getWearCount() != null && item.getWearCount() > 0) {
            BigDecimal costPerWear = item.getPrice().divide(
                    BigDecimal.valueOf(item.getWearCount()), 2, RoundingMode.HALF_UP);
            vo.setCostPerWear(costPerWear);
        }

        // 计算沉睡天数
        if (item.getLastWorn() != null) {
            int sleepingDays = (int) ChronoUnit.DAYS.between(item.getLastWorn(), LocalDate.now());
            vo.setSleepingDays(sleepingDays);
        } else {
            vo.setSleepingDays(null);
        }

        // 查询关联的搭配
        List<Outfit> outfits = outfitMapper.findByItemId(id);
        List<ItemVO.OutfitVO> outfitVOs = outfits.stream().map(o -> {
            ItemVO.OutfitVO outfitVO = new ItemVO.OutfitVO();
            outfitVO.setId(o.getId());
            outfitVO.setName(o.getName());
            outfitVO.setImageUrl(o.getImageUrl());
            outfitVO.setWearCount(o.getWearCount());
            return outfitVO;
        }).collect(Collectors.toList());
        vo.setOutfits(outfitVOs);

        // 缓存
        redisTemplate.opsForValue().set(cacheKey, vo, 1, TimeUnit.HOURS);

        return vo;
    }

    @Transactional
    public Long createItem(Long userId, ItemCreateDTO dto) {
        Item item = new Item();
        item.setUserId(userId);
        item.setName(dto.getName());
        item.setCategoryId(dto.getCategoryId());
        item.setSubCategoryId(dto.getSubCategoryId());
        item.setColors(dto.getColors() != null ? JSON.toJSONString(dto.getColors()) : null);
        item.setSize(dto.getSize());
        item.setMaterial(dto.getMaterial());
        item.setSeasons(dto.getSeasons() != null ? JSON.toJSONString(dto.getSeasons()) : null);
        item.setStyles(dto.getStyles() != null ? JSON.toJSONString(dto.getStyles()) : null);
        item.setOccasions(dto.getOccasions() != null ? JSON.toJSONString(dto.getOccasions()) : null);
        item.setImageUrl(dto.getImageUrl());
        item.setOriginalImageUrl(dto.getOriginalImageUrl());
        item.setLocationId(dto.getLocationId());
        item.setPrice(dto.getPrice());
        item.setPlatform(dto.getPlatform());
        item.setPurchaseDate(dto.getPurchaseDate());
        item.setStatus(1);
        item.setWearCount(0);

        itemMapper.insert(item);
        return item.getId();
    }

    @Transactional
    public void updateItem(Long userId, Long id, ItemCreateDTO dto) {
        Item item = itemMapper.selectById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException(20001, "单品不存在");
        }

        item.setName(dto.getName());
        item.setCategoryId(dto.getCategoryId());
        item.setSubCategoryId(dto.getSubCategoryId());
        item.setColors(dto.getColors() != null ? JSON.toJSONString(dto.getColors()) : null);
        item.setSize(dto.getSize());
        item.setMaterial(dto.getMaterial());
        item.setSeasons(dto.getSeasons() != null ? JSON.toJSONString(dto.getSeasons()) : null);
        item.setStyles(dto.getStyles() != null ? JSON.toJSONString(dto.getStyles()) : null);
        item.setOccasions(dto.getOccasions() != null ? JSON.toJSONString(dto.getOccasions()) : null);
        item.setImageUrl(dto.getImageUrl());
        item.setOriginalImageUrl(dto.getOriginalImageUrl());
        item.setLocationId(dto.getLocationId());
        item.setPrice(dto.getPrice());
        item.setPlatform(dto.getPlatform());
        item.setPurchaseDate(dto.getPurchaseDate());

        itemMapper.updateById(item);

        // 清除缓存
        redisTemplate.delete(ITEM_DETAIL_KEY + id);
    }

    @Transactional
    public void deleteItem(Long userId, Long id) {
        Item item = itemMapper.selectById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException(20001, "单品不存在");
        }

        itemMapper.deleteById(id);
        redisTemplate.delete(ITEM_DETAIL_KEY + id);
    }

    @Transactional
    public void recordWear(Long userId, Long id, WearRecordDTO dto) {
        Item item = itemMapper.selectById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException(20001, "单品不存在");
        }

        // 记录穿着
        WearRecord record = new WearRecord();
        record.setUserId(userId);
        record.setItemId(id);
        record.setWearDate(dto.getWearDate());
        record.setOccasion(dto.getOccasion());
        record.setNote(dto.getNote());
        wearRecordMapper.insert(record);

        // 更新单品穿着次数
        itemMapper.incrementWearCount(id, dto.getWearDate());

        // 清除缓存
        redisTemplate.delete(ITEM_DETAIL_KEY + id);
    }

    public void updateStatus(Long userId, Long id, Integer status) {
        Item item = itemMapper.selectById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException(20001, "单品不存在");
        }

        item.setStatus(status);
        itemMapper.updateById(item);
        redisTemplate.delete(ITEM_DETAIL_KEY + id);
    }

    public List<WearRecordVO> getWearRecords(Long userId, Long itemId) {
        List<WearRecord> records = wearRecordMapper.findByItemId(itemId);
        return records.stream().map(this::convertWearRecordToVO).collect(Collectors.toList());
    }

    private ItemVO convertToVO(Item item) {
        ItemVO vo = new ItemVO();
        vo.setId(item.getId());
        vo.setName(item.getName());
        vo.setCategoryId(item.getCategoryId());
        vo.setCategoryName(item.getCategoryName());
        vo.setSubCategoryId(item.getSubCategoryId());
        vo.setSubCategoryName(item.getSubCategoryName());
        vo.setColors(item.getColorList());
        vo.setSize(item.getSize());
        vo.setMaterial(item.getMaterial());
        vo.setStyles(item.getStyleList());
        vo.setSeasons(item.getSeasonList());
        vo.setOccasions(item.getOccasionList());
        vo.setImageUrl(item.getImageUrl());
        vo.setOriginalImageUrl(item.getOriginalImageUrl());
        vo.setStatus(item.getStatus());
        vo.setWearCount(item.getWearCount());
        vo.setLastWorn(item.getLastWorn());
        vo.setCreatedAt(item.getCreatedAt());
        vo.setUpdatedAt(item.getUpdatedAt());

        // 位置信息
        if (item.getLocationId() != null) {
            ItemVO.LocationVO locationVO = new ItemVO.LocationVO();
            locationVO.setId(item.getLocationId());
            locationVO.setName(item.getLocationName());
            vo.setLocation(locationVO);
        }

        // 购买信息
        if (item.getPrice() != null || item.getPlatform() != null || item.getPurchaseDate() != null) {
            ItemVO.PurchaseInfoVO purchaseInfo = new ItemVO.PurchaseInfoVO();
            purchaseInfo.setPrice(item.getPrice());
            purchaseInfo.setPlatform(item.getPlatform());
            purchaseInfo.setDate(item.getPurchaseDate());
            vo.setPurchaseInfo(purchaseInfo);
        }

        return vo;
    }

    private WearRecordVO convertWearRecordToVO(WearRecord record) {
        WearRecordVO vo = new WearRecordVO();
        vo.setId(record.getId());
        vo.setWearDate(record.getWearDate());
        vo.setWeatherTemp(record.getWeatherTemp());
        vo.setWeatherDesc(record.getWeatherDesc());
        vo.setOccasion(record.getOccasion());
        vo.setNote(record.getNote());
        return vo;
    }
}
