-- 智能衣柜小程序数据库初始化脚本
-- 版本: v1.0
-- 日期: 2026-04-13

-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_wardrobe
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_wardrobe;

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL COMMENT '主键，雪花算法',
    `openid` VARCHAR(64) NOT NULL COMMENT '微信openid',
    `union_id` VARCHAR(64) DEFAULT NULL COMMENT '微信unionId',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `avatar_url` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0未知，1男，2女',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1正常',
    `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 分类表
-- ============================================
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `name` VARCHAR(32) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父分类ID',
    `level` TINYINT NOT NULL DEFAULT 1 COMMENT '层级：1主分类，2子分类',
    `icon` VARCHAR(256) DEFAULT NULL COMMENT '图标URL',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- 初始化分类数据
INSERT INTO `category` (`id`, `name`, `parent_id`, `level`, `sort_order`) VALUES
(1, '上装', NULL, 1, 1),
(2, '下装', NULL, 1, 2),
(3, '鞋履', NULL, 1, 3),
(4, '配饰', NULL, 1, 4);

-- 初始化子分类数据
INSERT INTO `category` (`id`, `name`, `parent_id`, `level`, `sort_order`) VALUES
-- 上装子分类
(101, 'T恤', 1, 2, 1),
(102, '衬衫', 1, 2, 2),
(103, '卫衣', 1, 2, 3),
(104, '毛衣', 1, 2, 4),
(105, '针织衫', 1, 2, 5),
(106, '西装外套', 1, 2, 6),
(107, '夹克', 1, 2, 7),
(108, '羽绒服', 1, 2, 8),
(109, '大衣', 1, 2, 9),
-- 下装子分类
(201, '牛仔裤', 2, 2, 1),
(202, '休闲裤', 2, 2, 2),
(203, '西裤', 2, 2, 3),
(204, '短裤', 2, 2, 4),
(205, '半身裙', 2, 2, 5),
(206, '连衣裙', 2, 2, 6),
(207, '阔腿裤', 2, 2, 7),
-- 鞋履子分类
(301, '运动鞋', 3, 2, 1),
(302, '休闲鞋', 3, 2, 2),
(303, '皮鞋', 3, 2, 3),
(304, '靴子', 3, 2, 4),
(305, '凉鞋', 3, 2, 5),
(306, '拖鞋', 3, 2, 6),
-- 配饰子分类
(401, '包', 4, 2, 1),
(402, '帽子', 4, 2, 2),
(403, '围巾', 4, 2, 3),
(404, '腰带', 4, 2, 4),
(405, '首饰', 4, 2, 5),
(406, '眼镜', 4, 2, 6);

-- ============================================
-- 单品表
-- ============================================
CREATE TABLE IF NOT EXISTS `item` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(128) NOT NULL COMMENT '单品名称',
    `category_id` BIGINT NOT NULL COMMENT '主分类ID',
    `sub_category_id` BIGINT DEFAULT NULL COMMENT '子分类ID',
    `colors` JSON DEFAULT NULL COMMENT '颜色数组，如["白色","黑色"]',
    `size` VARCHAR(16) DEFAULT NULL COMMENT '尺码',
    `material` VARCHAR(64) DEFAULT NULL COMMENT '材质',
    `seasons` JSON DEFAULT NULL COMMENT '适用季节数组',
    `styles` JSON DEFAULT NULL COMMENT '风格标签数组',
    `occasions` JSON DEFAULT NULL COMMENT '适用场合数组',
    `image_url` VARCHAR(512) NOT NULL COMMENT '单品图片URL（去背景后）',
    `original_image_url` VARCHAR(512) DEFAULT NULL COMMENT '原始图片URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1在用，2已下架，3已捐赠，4已丢弃',
    `wear_count` INT NOT NULL DEFAULT 0 COMMENT '穿着次数',
    `last_worn` DATE DEFAULT NULL COMMENT '最后穿着日期',
    `location_id` BIGINT DEFAULT NULL COMMENT '存放位置ID',
    `price` DECIMAL(10,2) DEFAULT NULL COMMENT '价格',
    `platform` VARCHAR(32) DEFAULT NULL COMMENT '购买平台',
    `purchase_date` DATE DEFAULT NULL COMMENT '购买日期',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_last_worn` (`last_worn`),
    KEY `idx_user_category` (`user_id`, `category_id`),
    KEY `idx_user_category_status` (`user_id`, `category_id`, `status`),
    KEY `idx_user_last_worn` (`user_id`, `last_worn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='单品表';

-- ============================================
-- 存放位置表
-- ============================================
CREATE TABLE IF NOT EXISTS `location` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(64) NOT NULL COMMENT '位置名称',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父位置ID',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='存放位置表';

-- ============================================
-- 搭配表
-- ============================================
CREATE TABLE IF NOT EXISTS `outfit` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(128) DEFAULT NULL COMMENT '搭配名称',
    `description` VARCHAR(512) DEFAULT NULL COMMENT '搭配描述',
    `style` VARCHAR(32) DEFAULT NULL COMMENT '风格',
    `occasion` VARCHAR(32) DEFAULT NULL COMMENT '场合',
    `season` VARCHAR(32) DEFAULT NULL COMMENT '季节',
    `image_url` VARCHAR(512) DEFAULT NULL COMMENT '搭配预览图URL',
    `is_recommended` TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐生成：0用户创建，1系统推荐',
    `wear_count` INT NOT NULL DEFAULT 0 COMMENT '穿着次数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_recommended` (`is_recommended`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搭配表';

-- ============================================
-- 搭配单品关联表
-- ============================================
CREATE TABLE IF NOT EXISTS `outfit_item` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `outfit_id` BIGINT NOT NULL COMMENT '搭配ID',
    `item_id` BIGINT NOT NULL COMMENT '单品ID',
    `position` TINYINT NOT NULL COMMENT '位置：1上装，2下装，3鞋履，4配饰',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_outfit_item` (`outfit_id`, `item_id`),
    KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搭配单品关联表';

-- ============================================
-- 穿着记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `wear_record` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `item_id` BIGINT DEFAULT NULL COMMENT '单品ID（单品穿着时）',
    `outfit_id` BIGINT DEFAULT NULL COMMENT '搭配ID（整套穿着时）',
    `wear_date` DATE NOT NULL COMMENT '穿着日期',
    `weather_temp` DECIMAL(4,1) DEFAULT NULL COMMENT '当日温度',
    `weather_desc` VARCHAR(32) DEFAULT NULL COMMENT '天气描述',
    `occasion` VARCHAR(32) DEFAULT NULL COMMENT '场合',
    `note` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_outfit_id` (`outfit_id`),
    KEY `idx_wear_date` (`wear_date`),
    KEY `idx_user_wear_date` (`user_id`, `wear_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='穿着记录表';

-- ============================================
-- 推荐反馈表
-- ============================================
CREATE TABLE IF NOT EXISTS `feedback` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `outfit_id` BIGINT NOT NULL COMMENT '搭配ID',
    `type` TINYINT NOT NULL COMMENT '类型：1喜欢，2不喜欢',
    `reason` VARCHAR(128) DEFAULT NULL COMMENT '原因',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_outfit_id` (`outfit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐反馈表';

-- ============================================
-- 订单导入记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `order_import` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `image_url` VARCHAR(512) NOT NULL COMMENT '订单截图URL',
    `ocr_result` JSON DEFAULT NULL COMMENT 'OCR识别结果',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1处理中，2成功，3失败',
    `item_id` BIGINT DEFAULT NULL COMMENT '生成的单品ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单导入记录表';

-- ============================================
-- 标签表
-- ============================================
CREATE TABLE IF NOT EXISTS `tag` (
    `id`         BIGINT       NOT NULL COMMENT '主键',
    `type`       VARCHAR(16)  NOT NULL COMMENT '标签类型：style|season|occasion|color',
    `code`       VARCHAR(32)  NOT NULL COMMENT '标签编码（英文，唯一）',
    `name`       VARCHAR(32)  NOT NULL COMMENT '标签中文名称',
    `color_hex`  VARCHAR(16)  DEFAULT NULL COMMENT '颜色值（仅 color 类型使用）',
    `sort_order` INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_code` (`type`, `code`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 初始化风格标签
INSERT INTO `tag` (`id`, `type`, `code`, `name`, `sort_order`) VALUES
(1001, 'style', 'casual',     '休闲', 1),
(1002, 'style', 'business',   '商务', 2),
(1003, 'style', 'sport',      '运动', 3),
(1004, 'style', 'formal',     '正式', 4),
(1005, 'style', 'vintage',    '复古', 5),
(1006, 'style', 'minimalist', '简约', 6),
(1007, 'style', 'street',     '街头', 7),
(1008, 'style', 'elegant',    '优雅', 8);

-- 初始化季节标签
INSERT INTO `tag` (`id`, `type`, `code`, `name`, `sort_order`) VALUES
(2001, 'season', 'spring', '春',      1),
(2002, 'season', 'summer', '夏',      2),
(2003, 'season', 'autumn', '秋',      3),
(2004, 'season', 'winter', '冬',      4),
(2005, 'season', 'all',    '四季通用', 5);

-- 初始化场合标签
INSERT INTO `tag` (`id`, `type`, `code`, `name`, `sort_order`) VALUES
(3001, 'occasion', 'commute', '通勤',   1),
(3002, 'occasion', 'date',    '约会',   2),
(3003, 'occasion', 'sport',   '运动',   3),
(3004, 'occasion', 'party',   '聚会',   4),
(3005, 'occasion', 'travel',  '旅行',   5),
(3006, 'occasion', 'home',    '居家',   6),
(3007, 'occasion', 'formal',  '正式场合', 7);

-- 初始化颜色标签
INSERT INTO `tag` (`id`, `type`, `code`, `name`, `color_hex`, `sort_order`) VALUES
(4001, 'color', 'black',  '黑色',    '#000000', 1),
(4002, 'color', 'white',  '白色',    '#FFFFFF', 2),
(4003, 'color', 'gray',   '灰色',    '#808080', 3),
(4004, 'color', 'brown',  '棕色',    '#8B4513', 4),
(4005, 'color', 'blue',   '蓝色',    '#0000FF', 5),
(4006, 'color', 'green',  '绿色',    '#008000', 6),
(4007, 'color', 'red',    '红色',    '#FF0000', 7),
(4008, 'color', 'yellow', '黄色',    '#FFFF00', 8),
(4009, 'color', 'purple', '紫色',    '#800080', 9),
(4010, 'color', 'pink',   '粉色',    '#FFC0CB', 10),
(4011, 'color', 'orange', '橙色',    '#FFA500', 11),
(4012, 'color', 'multi',  '多色/图案', NULL,    12);
