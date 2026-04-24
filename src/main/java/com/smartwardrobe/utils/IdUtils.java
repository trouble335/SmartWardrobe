package com.smartwardrobe.utils;

import java.util.UUID;

public class IdUtils {

    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateShortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static Long generateSnowflakeId() {
        // 简化的ID生成，实际项目中应该使用雪花算法
        return System.currentTimeMillis() * 10000 + (long) (Math.random() * 10000);
    }
}
