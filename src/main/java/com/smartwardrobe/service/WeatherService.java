package com.smartwardrobe.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.smartwardrobe.exception.BusinessException;
import com.smartwardrobe.vo.WeatherVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${qweather.key:}")
    private String apiKey;

    @Value("${qweather.base-url:https://devapi.qweather.com/v7}")
    private String baseUrl;

    private static final String WEATHER_CACHE_KEY = "weather:";
    private static final long WEATHER_CACHE_TTL = 30;

    public WeatherVO getWeather(String location) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("天气API key未配置，返回默认天气");
            return getDefaultWeather();
        }

        String cacheKey = WEATHER_CACHE_KEY + location;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (WeatherVO) cached;
        }

        try {
            String url = baseUrl + "/weather/now?location=" + location + "&key=" + apiKey;
            String response = restTemplate.getForObject(url, String.class);
            log.debug("天气API响应: {}", response);

            JSONObject json = JSON.parseObject(response);
            if (!"200".equals(json.getString("code"))) {
                log.warn("天气API返回错误: {}", json.getString("code"));
                return getDefaultWeather();
            }

            JSONObject now = json.getJSONObject("now");
            WeatherVO weather = new WeatherVO();
            weather.setTemp(new BigDecimal(now.getString("temp")));
            weather.setDescription(now.getString("text"));
            weather.setHumidity(Integer.parseInt(now.getString("humidity")));
            weather.setWindSpeed(now.getString("windSpeed"));

            // 根据温度获取穿搭建议
            weather.setSuggestion(getOutfitSuggestion(weather.getTemp().intValue()));

            // 缓存
            redisTemplate.opsForValue().set(cacheKey, weather, WEATHER_CACHE_TTL, TimeUnit.MINUTES);

            return weather;

        } catch (Exception e) {
            log.error("获取天气失败", e);
            return getDefaultWeather();
        }
    }

    public String getOutfitSuggestion(int temperature) {
        if (temperature < 5) {
            return "寒冷，建议穿羽绒服、厚大衣";
        } else if (temperature < 10) {
            return "较冷，建议穿大衣、棉服";
        } else if (temperature < 15) {
            return "凉爽，建议穿夹克、风衣";
        } else if (temperature < 20) {
            return "舒适，建议穿薄外套";
        } else if (temperature < 25) {
            return "温暖，建议穿长袖或薄外套";
        } else if (temperature < 30) {
            return "较热，建议穿短袖、短裤";
        } else {
            return "炎热，建议穿轻薄透气衣物";
        }
    }

    private WeatherVO getDefaultWeather() {
        WeatherVO weather = new WeatherVO();
        weather.setTemp(new BigDecimal("18"));
        weather.setDescription("多云");
        weather.setHumidity(60);
        weather.setWindSpeed("10");
        weather.setSuggestion("舒适，建议穿薄外套");
        return weather;
    }
}
