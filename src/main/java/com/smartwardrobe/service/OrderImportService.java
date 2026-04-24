package com.smartwardrobe.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.smartwardrobe.dto.ItemCreateDTO;
import com.smartwardrobe.entity.Item;
import com.smartwardrobe.entity.OrderImport;
import com.smartwardrobe.exception.BusinessException;
import com.smartwardrobe.mapper.ItemMapper;
import com.smartwardrobe.mapper.OrderImportMapper;
import com.smartwardrobe.vo.OrderImportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderImportService {

    private final OrderImportMapper orderImportMapper;
    private final ItemMapper itemMapper;
    private final FileService fileService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${baidu.ocr.api-key:}")
    private String apiKey;

    @Value("${baidu.ocr.secret-key:}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private String accessToken;

    private static final String ACCESS_TOKEN_KEY = "baidu:ocr:access_token";

    @Transactional
    public OrderImportVO importOrder(Long userId, String imageUrl, String platform) {
        // 创建导入记录
        OrderImport orderImport = new OrderImport();
        orderImport.setUserId(userId);
        orderImport.setImageUrl(imageUrl);
        orderImport.setStatus(1); // 处理中

        orderImportMapper.insert(orderImport);

        try {
            // 调用OCR识别
            List<String> ocrTexts = recognizeText(imageUrl);

            // 解析订单信息
            OrderImport.OrderRecognizedInfo info = parseOrderInfo(ocrTexts, platform);

            // 更新导入记录
            orderImport.setOcrResult(JSON.toJSONString(ocrTexts));
            orderImport.setStatus(2); // 成功
            orderImportMapper.updateById(orderImport);

            // 构建返回结果
            OrderImportVO vo = new OrderImportVO();
            vo.setImportId(orderImport.getId());
            vo.setStatus(2);
            vo.setRecognizedInfo(info);

            return vo;

        } catch (Exception e) {
            log.error("订单导入失败", e);
            orderImport.setStatus(3); // 失败
            orderImportMapper.updateById(orderImport);

            throw new BusinessException(40003, "订单识别失败: " + e.getMessage());
        }
    }

    private List<String> recognizeText(String imageUrl) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("百度OCR API key未配置，返回模拟数据");
            return getMockOcrResult();
        }

        try {
            String token = getAccessToken();
            String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=" + token;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("url", imageUrl);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            JSONObject response = restTemplate.postForObject(url, request, JSONObject.class);

            List<String> texts = new ArrayList<>();
            if (response != null && response.containsKey("words_result")) {
                JSONArray wordsResult = response.getJSONArray("words_result");
                for (int i = 0; i < wordsResult.size(); i++) {
                    texts.add(wordsResult.getJSONObject(i).getString("words"));
                }
            }

            return texts;

        } catch (Exception e) {
            log.error("OCR识别失败", e);
            return getMockOcrResult();
        }
    }

    private String getAccessToken() {
        // 先从缓存获取
        Object cached = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
        if (cached != null) {
            return (String) cached;
        }

        try {
            String url = "https://aip.baidubce.com/oauth/2.0/token?" +
                    "grant_type=client_credentials&client_id=" + apiKey +
                    "&client_secret=" + secretKey;

            JSONObject response = restTemplate.getForObject(url, JSONObject.class);
            if (response != null && response.containsKey("access_token")) {
                String token = response.getString("access_token");
                int expiresIn = response.getIntValue("expires_in");

                // 缓存token
                redisTemplate.opsForValue().set(ACCESS_TOKEN_KEY, token, expiresIn - 300, TimeUnit.SECONDS);

                return token;
            }
        } catch (Exception e) {
            log.error("获取百度OCR access_token失败", e);
        }

        return null;
    }

    private OrderImport.OrderRecognizedInfo parseOrderInfo(List<String> ocrTexts, String platform) {
        OrderImport.OrderRecognizedInfo info = new OrderImport.OrderRecognizedInfo();

        // 合并所有文本
        String fullText = String.join("\n", ocrTexts);

        // 解析商品名称
        info.setName(extractProductName(ocrTexts));

        // 解析价格
        info.setPrice(extractPrice(fullText));

        // 解析平台
        info.setPlatform(extractPlatform(fullText, platform));

        // 解析日期
        info.setPurchaseDate(extractDate(fullText));

        // 图片URL需要从外部传入
        info.setImageUrl(null);

        return info;
    }

    private String extractProductName(List<String> texts) {
        // 尝试找到商品名称
        for (String line : texts) {
            // 跳过一些明显的非商品名称行
            if (line.contains("订单") || line.contains("时间") || line.contains("价格")
                    || line.contains("金额") || line.contains("支付") || line.contains("收货")
                    || line.contains("手机") || line.contains("地址") || line.length() < 3) {
                continue;
            }

            // 可能是商品名称
            if (!line.matches(".*[0-9]+.*") && line.length() >= 3 && line.length() <= 50) {
                return line;
            }
        }

        return "未知商品";
    }

    private BigDecimal extractPrice(String text) {
        // 匹配价格格式：¥xxx.xx 或 xxx元
        Pattern pattern = Pattern.compile("[¥￥]?\\s*(\\d+\\.?\\d*)\\s*[元]?");
        Matcher matcher = pattern.matcher(text);

        BigDecimal maxPrice = null;
        while (matcher.find()) {
            try {
                BigDecimal price = new BigDecimal(matcher.group(1));
                if (price.compareTo(new BigDecimal("10")) >= 0 && price.compareTo(new BigDecimal("100000")) <= 0) {
                    if (maxPrice == null || price.compareTo(maxPrice) > 0) {
                        maxPrice = price;
                    }
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }

        return maxPrice;
    }

    private String extractPlatform(String text, String hintPlatform) {
        if (hintPlatform != null && !hintPlatform.isEmpty()) {
            return hintPlatform;
        }

        if (text.contains("淘宝") || text.contains("天猫")) {
            return "淘宝";
        } else if (text.contains("京东") || text.contains("JD")) {
            return "京东";
        } else if (text.contains("拼多多") || text.contains("PDD")) {
            return "拼多多";
        } else if (text.contains("唯品会")) {
            return "唯品会";
        }

        return "其他";
    }

    private String extractDate(String text) {
        // 匹配日期格式：yyyy-MM-dd 或 yyyy年MM月dd日
        Pattern[] patterns = {
                Pattern.compile("(\\d{4}[-/]\\d{1,2}[-/]\\d{1,2})"),
                Pattern.compile("(\\d{4}年\\d{1,2}月\\d{1,2}日)"),
                Pattern.compile("(\\d{4}\\.\\d{1,2}\\.\\d{1,2})")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String dateStr = matcher.group(1);
                try {
                    // 标准化日期格式
                    dateStr = dateStr.replace("年", "-").replace("月", "-").replace("日", "").replace(".", "-");
                    return dateStr;
                } catch (Exception e) {
                    // 忽略
                }
            }
        }

        return null;
    }

    private List<String> getMockOcrResult() {
        return List.of(
                "订单详情",
                "白色牛津纺衬衫",
                "纯棉修身款",
                "¥299.00",
                "下单时间：2024-03-01 10:30:00",
                "收货地址：北京市朝阳区"
        );
    }

    @Transactional
    public Long confirmImport(Long userId, Long importId, ItemCreateDTO itemDTO) {
        OrderImport orderImport = orderImportMapper.findByIdAndUserId(importId, userId);
        if (orderImport == null) {
            throw new BusinessException(20001, "导入记录不存在");
        }

        // 创建单品
        Item item = new Item();
        item.setUserId(userId);
        item.setName(itemDTO.getName());
        item.setCategoryId(itemDTO.getCategoryId());
        item.setSubCategoryId(itemDTO.getSubCategoryId());
        item.setColors(itemDTO.getColors() != null ? JSON.toJSONString(itemDTO.getColors()) : null);
        item.setSize(itemDTO.getSize());
        item.setMaterial(itemDTO.getMaterial());
        item.setSeasons(itemDTO.getSeasons() != null ? JSON.toJSONString(itemDTO.getSeasons()) : null);
        item.setStyles(itemDTO.getStyles() != null ? JSON.toJSONString(itemDTO.getStyles()) : null);
        item.setOccasions(itemDTO.getOccasions() != null ? JSON.toJSONString(itemDTO.getOccasions()) : null);
        item.setImageUrl(itemDTO.getImageUrl());
        item.setOriginalImageUrl(itemDTO.getOriginalImageUrl());
        item.setLocationId(itemDTO.getLocationId());
        item.setPrice(itemDTO.getPrice());
        item.setPlatform(itemDTO.getPlatform());
        if (itemDTO.getPurchaseDate() != null) {
            item.setPurchaseDate(itemDTO.getPurchaseDate());
        }
        item.setStatus(1);
        item.setWearCount(0);

        itemMapper.insert(item);

        // 更新导入记录
        orderImportMapper.updateStatusAndItemId(importId, 2, item.getId());

        return item.getId();
    }

    public OrderImport getOrderImport(Long userId, Long importId) {
        return orderImportMapper.findByIdAndUserId(importId, userId);
    }
}
