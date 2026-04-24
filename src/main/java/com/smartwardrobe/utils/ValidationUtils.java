package com.smartwardrobe.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public static boolean isValidPhone(String phone) {
        return StringUtils.hasText(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidEmail(String email) {
        return StringUtils.hasText(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    public static boolean isValidName(String name) {
        return StringUtils.hasText(name) && name.length() <= 128;
    }

    public static boolean isValidPrice(java.math.BigDecimal price) {
        return price != null && price.compareTo(java.math.BigDecimal.ZERO) >= 0;
    }
}
