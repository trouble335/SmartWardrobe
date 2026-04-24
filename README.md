# 智能衣柜小程序后端服务

## 项目概述

智能衣柜小程序后端服务，基于 Spring Boot 3.2 + MyBatis-Plus + MySQL 8.0 + Redis 构建，提供单品管理、穿搭推荐、数据统计等功能。

## 技术栈

- **Java 17** - LTS版本
- **Spring Boot 3.2** - 核心框架
- **MyBatis-Plus 3.5** - ORM框架
- **MySQL 8.0** - 关系型数据库
- **Redis 7.0** - 缓存
- **阿里云OSS** - 对象存储
- **JWT** - 身份认证

## 项目结构

```
smart-wardrobe-server/
├── pom.xml                           # Maven配置
├── src/
│   ├── main/
│   │   ├── java/com/smartwardrobe/
│   │   │   ├── aspect/               # 切面
│   │   │   ├── config/               # 配置类
│   │   │   ├── constant/             # 常量定义
│   │   │   ├── controller/           # 控制器
│   │   │   ├── dto/                  # 数据传输对象
│   │   │   ├── entity/               # 实体类
│   │   │   ├── exception/            # 异常处理
│   │   │   ├── interactor/           # 拦截器
│   │   │   ├── mapper/               # MyBatis Mapper
│   │   │   ├── service/              # 业务逻辑
│   │   │   ├── utils/                # 工具类
│   │   │   └── vo/                   # 视图对象
│   │   └── resources/
│   │       ├── application.yml       # 主配置
│   │       ├── application-dev.yml   # 开发环境配置
│   │       ├── db/init.sql           # 数据库初始化脚本
│   │       └── mapper/               # MyBatis XML映射
│   └── test/                         # 测试目录
```

## API 接口

### 认证模块 `/auth`
- `POST /auth/wechat/login` - 微信登录

### 用户模块 `/user`
- `GET /user/info` - 获取用户信息
- `PUT /user/info` - 更新用户信息

### 单品模块 `/items`
- `GET /items` - 获取单品列表（支持筛选、排序、分页）
- `GET /items/{id}` - 获取单品详情
- `POST /items` - 创建单品
- `PUT /items/{id}` - 更新单品
- `DELETE /items/{id}` - 删除单品
- `POST /items/{id}/wear` - 记录穿着
- `PUT /items/{id}/status` - 更新状态
- `GET /items/{id}/wear-records` - 获取穿着记录

### 分类模块 `/categories`
- `GET /categories` - 获取分类树

### 标签模块 `/tags`
- `GET /tags?type={style|season|occasion|color}` - 获取标签列表

### 位置模块 `/locations`
- `GET /locations` - 获取位置列表
- `POST /locations` - 创建位置
- `PUT /locations/{id}` - 更新位置
- `DELETE /locations/{id}` - 删除位置

### 文件模块 `/files`
- `POST /files/upload` - 上传图片
- `POST /files/remove-background` - 图片去背景

### 推荐模块 `/recommend`
- `GET /recommend/outfits` - 获取穿搭推荐
- `POST /recommend/outfits/{id}/feedback` - 提交反馈
- `POST /recommend/outfits/{id}/wear` - 记录穿搭穿着

### 统计模块 `/statistics`
- `GET /statistics/report` - 获取衣橱报告
- `GET /statistics/sleeping-items` - 获取沉睡单品列表

### 订单导入模块 `/orders`
- `POST /orders/import` - 导入订单截图
- `POST /orders/import/{id}/confirm` - 确认导入

## 快速开始

### 1. 环境准备
- JDK 17+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.8+

### 2. 数据库初始化
```bash
mysql -u root -p < src/main/resources/db/init.sql
```

### 3. 配置修改
修改 `application-dev.yml` 中的数据库、Redis、第三方服务配置

### 4. 启动服务
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 5. 测试接口
访问 `http://localhost:8081/api/v1/categories` 验证服务是否正常

## 第三方服务配置

### 微信小程序
```yaml
wechat:
  appid: your_appid
  secret: your_secret
```

### 和风天气
```yaml
qweather:
  key: your_api_key
```

### 百度OCR
```yaml
baidu:
  ocr:
    app-id: your_app_id
    api-key: your_api_key
    secret-key: your_secret_key
```

### Remove.bg
```yaml
removebg:
  api-key: your_api_key
```

### 阿里云OSS
```yaml
aliyun:
  oss:
    endpoint: your_endpoint
    access-key-id: your_access_key
    access-key-secret: your_secret
    bucket-name: your_bucket
```

## 核心功能说明

### 穿搭推荐算法
推荐算法基于以下规则：
1. **天气过滤** - 根据温度筛选适用季节的单品
2. **场合匹配** - 优先匹配用户选择的场合标签
3. **颜色搭配** - 基于配色规则（同色系、邻近色、中性色搭配）
4. **穿着频率** - 优先推荐近期穿着较少的单品
5. **完整性** - 确保上装+下装+鞋履的基础组合

### 温度-季节映射
| 温度范围 | 推荐季节 |
|---------|---------|
| < 5°C | 冬 |
| 5-10°C | 冬、秋 |
| 10-15°C | 秋、春 |
| 15-20°C | 春、秋 |
| 20-25°C | 春、夏、四季通用 |
| > 25°C | 夏、四季通用 |

### 颜色搭配规则
- **同色系** (90分) - 相同颜色不同深浅
- **中性色搭配** (85分) - 黑、白、灰、棕与任意颜色
- **邻近色搭配** (80分) - 色相环相邻颜色
- **对比色搭配** (60分) - 需谨慎使用

## 响应格式

### 成功响应
```json
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "timestamp": 1712736000000
}
```

### 错误响应
```json
{
  "code": 20001,
  "message": "单品不存在",
  "data": null,
  "timestamp": 1712736000000
}
```

## 错误码

| 范围 | 说明 |
|-----|------|
| 0 | 成功 |
| 10001-10999 | 参数错误 |
| 20001-20999 | 业务错误 |
| 30001-30999 | 权限错误 |
| 40001-40999 | 第三方服务错误 |
| 50001-50999 | 系统错误 |
