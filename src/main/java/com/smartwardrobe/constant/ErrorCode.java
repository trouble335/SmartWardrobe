package com.smartwardrobe.constant;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // 成功
    SUCCESS(0, "success"),

    // 参数错误 10001-10999
    PARAM_ERROR(10001, "参数错误"),
    PARAM_MISSING(10002, "参数缺失"),
    PARAM_FORMAT_ERROR(10003, "参数格式错误"),

    // 业务错误 20001-20999
    ITEM_NOT_FOUND(20001, "单品不存在"),
    ITEM_DELETED(20002, "单品已删除"),
    WARDROBE_FULL(20003, "衣柜已满"),
    IMAGE_PROCESS_FAILED(20004, "图片处理失败"),
    OUTFIT_NOT_FOUND(20005, "搭配不存在"),
    LOCATION_NOT_FOUND(20006, "存放位置不存在"),
    CATEGORY_NOT_FOUND(20007, "分类不存在"),
    TAG_NOT_FOUND(20008, "标签不存在"),
    TAG_CODE_DUPLICATE(20009, "标签编码已存在"),

    // 权限错误 30001-30999
    NOT_LOGIN(30001, "未登录"),
    LOGIN_EXPIRED(30002, "登录过期"),
    NO_PERMISSION(30003, "无权限"),

    // 第三方服务错误 40001-40999
    WECHAT_API_ERROR(40001, "微信接口调用失败"),
    WEATHER_API_ERROR(40002, "天气接口调用失败"),
    OCR_ERROR(40003, "OCR识别失败"),
    REMOVE_BG_ERROR(40004, "图片去背景失败"),
    OSS_ERROR(40005, "文件上传失败"),

    // 系统错误 50001-50999
    SYSTEM_ERROR(50001, "系统繁忙"),
    DATABASE_ERROR(50002, "数据库错误");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
