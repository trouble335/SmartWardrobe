# 智能衣柜后端服务 — 环境配置指南

> 本文档涵盖项目启动所需的全部第三方服务与本地环境配置步骤。

---

## 目录

1. [运行环境要求](#1-运行环境要求)
2. [MySQL 数据库](#2-mysql-数据库)
3. [Redis 缓存](#3-redis-缓存)
4. [微信小程序](#4-微信小程序)
5. [和风天气 QWeather](#5-和风天气-qweather)
6. [百度 OCR](#6-百度-ocr)
7. [Remove.bg 抠图服务](#7-removebg-抠图服务)
8. [阿里云 OSS 对象存储](#8-阿里云-oss-对象存储)
9. [application-dev.yml 完整示例](#9-application-devyml-完整示例)
10. [生产环境变量清单](#10-生产环境变量清单)

---

## 1. 运行环境要求

| 组件 | 版本要求 |
|------|---------|
| JDK | 17+ |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |

---

## 2. MySQL 数据库

### 2.1 安装 MySQL 8.0

从 [MySQL 官网](https://dev.mysql.com/downloads/mysql/) 下载并安装 MySQL 8.0，安装时记录 `root` 账户密码。

### 2.2 初始化数据库

启动 MySQL 后，执行项目内置的初始化脚本：

```bash
mysql -u root -p < src/main/resources/db/init.sql
```

脚本将自动完成以下操作：
- 创建数据库 `smart_wardrobe`（字符集 utf8mb4）
- 创建全部业务表（user / item / outfit / wear_record 等共 9 张表）
- 插入初始分类数据（上装、下装、鞋履、配饰及其子分类）
- 插入初始标签数据（风格、季节、场合、颜色标签）

### 2.3 配置连接信息

在 `application-dev.yml` 中填写：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_wardrobe?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 你的MySQL密码
```

---

## 3. Redis 缓存

### 3.1 安装 Redis

**Windows（推荐使用 WSL 或 Docker）：**

```bash
# 使用 Docker 启动 Redis
docker run -d --name redis -p 6379:6379 redis:7.0 --requirepass 你的Redis密码
```

或从 [Redis 官网](https://redis.io/download/) 下载 Windows 版本安装包。

### 3.2 配置连接信息

在 `application-dev.yml` 中填写：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 你的Redis密码   # 若无密码则留空
    database: 0
```

### 3.3 验证连接

```bash
redis-cli -h localhost -p 6379 -a 你的Redis密码 ping
# 返回 PONG 表示连接成功
```

---

## 4. 微信小程序

### 4.1 注册微信小程序

1. 访问 [微信公众平台](https://mp.weixin.qq.com/) 并注册账号
2. 选择 **小程序** 类型进行注册
3. 完成主体信息认证

### 4.2 获取 AppID 和 AppSecret

1. 登录 [微信公众平台](https://mp.weixin.qq.com/)
2. 进入 **开发** → **开发管理** → **开发设置**
3. 找到 **AppID（小程序ID）** 和 **AppSecret（小程序密钥）**
4. AppSecret 需点击「生成」按钮获取，**请妥善保存，仅显示一次**

### 4.3 配置到项目

在 `application-dev.yml` 中填写：

```yaml
wechat:
  appid: wx你的AppID
  secret: 你的AppSecret
```

---

## 5. 和风天气 QWeather

> 用于穿搭推荐功能中的天气数据查询。

### 5.1 注册账号

1. 访问 [和风天气开发者平台](https://dev.qweather.com/)
2. 注册并登录账号

### 5.2 创建应用获取 API Key

1. 登录后进入 **控制台** → **项目管理**
2. 点击 **创建项目**，填写项目名称（如：SmartWardrobe）
3. 选择订阅类型：
   - **免费版**：每天 1000 次调用，足够开发测试使用
   - **商业版**：按量计费，生产环境使用
4. 创建完成后，在项目详情中找到 **API KEY**

### 5.3 配置到项目

在 `application-dev.yml` 中填写：

```yaml
qweather:
  key: 你的QWeather-API-Key
  base-url: https://devapi.qweather.com/v7   # 免费版使用 devapi，商业版改为 api
```

> **注意**：免费版 base-url 为 `https://devapi.qweather.com/v7`，商业版为 `https://api.qweather.com/v7`

---

## 6. 百度 OCR

> 用于订单截图识别（订单导入功能），自动从购物截图中提取商品信息。

### 6.1 注册百度智能云账号

1. 访问 [百度智能云](https://cloud.baidu.com/)
2. 注册并完成实名认证

### 6.2 创建 OCR 应用

1. 进入控制台，搜索 **文字识别 OCR**
2. 点击 **创建应用**，填写应用信息（应用名称、接口选择「通用文字识别」）
3. 创建完成后，在应用详情页获取以下三个参数：
   - **AppID**
   - **API Key**
   - **Secret Key**

### 6.3 开通免费额度

1. 在 **文字识别** 服务页面，点击 **立即使用**
2. 开通 **通用文字识别（标准版）** — 每月免费 1000 次

### 6.4 配置到项目

在 `application-dev.yml` 中填写：

```yaml
baidu:
  ocr:
    app-id: 你的AppID
    api-key: 你的API-Key
    secret-key: 你的Secret-Key
```

---

## 7. Remove.bg 抠图服务

> 用于上传单品图片时自动去除背景，保留服装主体。

### 7.1 注册账号

1. 访问 [remove.bg](https://www.remove.bg/)
2. 点击右上角 **Sign up** 注册账号（可使用 Google 账号登录）

### 7.2 获取 API Key

1. 登录后访问 [API 密钥页面](https://www.remove.bg/api)
2. 点击 **Get API Key** 或进入 **Account → API keys**
3. 复制生成的 API Key

> **免费额度**：每月 50 次免费调用（开发测试足够使用）

### 7.3 配置到项目

在 `application-dev.yml` 中填写：

```yaml
removebg:
  api-key: 你的RemoveBg-API-Key
  base-url: https://api.remove.bg/v1.0
```

---

## 8. 阿里云 OSS 对象存储

> 用于存储单品图片、头像等文件资源。

### 8.1 注册阿里云账号

1. 访问 [阿里云官网](https://www.aliyun.com/)
2. 注册并完成实名认证

### 8.2 开通 OSS 服务

1. 进入控制台，搜索 **对象存储 OSS**
2. 点击 **立即开通**（新用户有免费试用额度）

### 8.3 创建 Bucket

1. 进入 **OSS 控制台** → **Bucket 列表** → **创建 Bucket**
2. 填写配置：
   - **Bucket 名称**：如 `smart-wardrobe-images`（全局唯一）
   - **地域**：选择离用户最近的地区（如：华东1 上海）
   - **读写权限**：选择 **公共读**（方便前端直接访问图片 URL）
3. 记录所在地域对应的 **Endpoint**，如：`oss-cn-shanghai.aliyuncs.com`

### 8.4 创建 RAM 子账号并授权

> 不建议直接使用主账号 AccessKey，安全风险高。

1. 进入 **RAM 访问控制** → **用户管理** → **创建用户**
2. 勾选 **编程访问**，创建完成后保存 **AccessKey ID** 和 **AccessKey Secret**（仅显示一次）
3. 为该用户添加权限策略：`AliyunOSSFullAccess`（或仅授权该 Bucket 的自定义策略）

### 8.5 配置自定义域名（可选）

1. 在 Bucket 详情 → **域名管理** 中绑定自定义域名
2. 绑定完成后作为 `domain` 配置值，用于拼接图片访问 URL

### 8.6 配置到项目

在 `application-dev.yml` 中填写：

```yaml
aliyun:
  oss:
    endpoint: oss-cn-shanghai.aliyuncs.com       # 替换为你的地域节点
    access-key-id: 你的AccessKeyId
    access-key-secret: 你的AccessKeySecret
    bucket-name: smart-wardrobe-images           # 替换为你的Bucket名称
    domain: https://smart-wardrobe-images.oss-cn-shanghai.aliyuncs.com  # 或自定义域名
```

---

## 9. application-dev.yml 完整示例

将以下内容复制到 `src/main/resources/application-dev.yml`，并替换所有占位符：

```yaml
server:
  port: 8081
  servlet:
    context-path: /api/v1

spring:
  application:
    name: smart-wardrobe-server
  datasource:
    url: jdbc:mysql://localhost:3306/smart_wardrobe?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 你的MySQL密码
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: localhost
    port: 6379
    password: 你的Redis密码
    database: 0

jwt:
  secret: smartWardrobeSecretKey2024ForJwtTokenGenerationTest
  expiration: 7200000

wechat:
  appid: 你的微信AppID
  secret: 你的微信AppSecret

qweather:
  key: 你的QWeather-API-Key
  base-url: https://devapi.qweather.com/v7

baidu:
  ocr:
    app-id: 你的百度OCR-AppID
    api-key: 你的百度OCR-ApiKey
    secret-key: 你的百度OCR-SecretKey

removebg:
  api-key: 你的RemoveBg-API-Key
  base-url: https://api.remove.bg/v1.0

aliyun:
  oss:
    endpoint: oss-cn-shanghai.aliyuncs.com
    access-key-id: 你的AccessKeyId
    access-key-secret: 你的AccessKeySecret
    bucket-name: 你的BucketName
    domain: https://你的BucketName.oss-cn-shanghai.aliyuncs.com

logging:
  level:
    com.smartwardrobe: DEBUG
    org.springframework.web: INFO
```

---

## 10. 生产环境变量清单

生产环境通过环境变量注入敏感配置，`application.yml` 已支持以下环境变量：

| 环境变量 | 说明 | 示例值 |
|---------|------|-------|
| `MYSQL_HOST` | MySQL 主机地址 | `127.0.0.1` |
| `MYSQL_PORT` | MySQL 端口 | `3306` |
| `MYSQL_DB` | 数据库名称 | `smart_wardrobe` |
| `MYSQL_USER` | 数据库用户名 | `root` |
| `MYSQL_PASSWORD` | 数据库密码 | `your_password` |
| `REDIS_HOST` | Redis 主机地址 | `127.0.0.1` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | `your_password` |
| `REDIS_DB` | Redis 数据库编号 | `0` |
| `JWT_SECRET` | JWT 签名密钥 | `your_jwt_secret` |
| `JWT_EXPIRATION` | JWT 过期时间（毫秒） | `7200000` |
| `WECHAT_APPID` | 微信小程序 AppID | `wx...` |
| `WECHAT_SECRET` | 微信小程序 AppSecret | `...` |
| `QWEATHER_KEY` | 和风天气 API Key | `...` |
| `BAIDU_OCR_APP_ID` | 百度 OCR AppID | `...` |
| `BAIDU_OCR_API_KEY` | 百度 OCR API Key | `...` |
| `BAIDU_OCR_SECRET_KEY` | 百度 OCR Secret Key | `...` |
| `REMOVEBG_API_KEY` | Remove.bg API Key | `...` |
| `OSS_ENDPOINT` | 阿里云 OSS Endpoint | `oss-cn-shanghai.aliyuncs.com` |
| `OSS_ACCESS_KEY_ID` | 阿里云 AccessKey ID | `...` |
| `OSS_ACCESS_KEY_SECRET` | 阿里云 AccessKey Secret | `...` |
| `OSS_BUCKET_NAME` | OSS Bucket 名称 | `smart-wardrobe-images` |
| `OSS_DOMAIN` | OSS 访问域名 | `https://...` |

### 启动命令示例（生产环境）

```bash
java -jar smart-wardrobe-server-1.0.0.jar \
  --MYSQL_PASSWORD=your_password \
  --REDIS_PASSWORD=your_password \
  --WECHAT_APPID=wx... \
  --WECHAT_SECRET=... \
  --QWEATHER_KEY=... \
  --OSS_ACCESS_KEY_ID=... \
  --OSS_ACCESS_KEY_SECRET=...
```

---

> 所有密钥信息请妥善保管，**切勿提交至版本控制系统（Git）中**。
