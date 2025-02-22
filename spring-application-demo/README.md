# Spring Boot示例应用

这是一个基于Spring Boot的示例应用，用于演示和测试不同数据库在电商订单场景下的性能表现。

## 功能特性

- 订单管理系统的Web界面
  - 支持订单列表的展示和分页
  - 支持多维度筛选（店铺名称、订单状态、支付状态等）
  - 支持金额范围和时间范围筛选
  - 支持多字段排序
- 销售统计功能
  - 商品销量排行
  - 店铺销售统计

## 技术栈

### 后端
- Spring Boot 3.2.2
- Spring Data JPA
- MySQL/PostgreSQL/Doris/ClickHouse/Elasticsearch（可配置）
- Gradle

### 前端
- Vue 3
- Naive UI
- 原生JavaScript

## 快速开始

### 1. 环境要求
- JDK 17+
- Gradle
- MySQL 8.0+（或其他支持的数据库）

### 2. 数据库准备
1. 创建数据库
```sql
CREATE DATABASE mall4cloud_order;
```

2. 执行SQL脚本
```bash
# 进入data-generator目录
cd ../data-generator

# 执行数据库初始化脚本
mysql -u your_username -p mall4cloud_order < mall4cloud_order.sql

# 生成测试数据（可选）
python generate_order_data.py
```

### 3. 配置数据库连接
修改`src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mall4cloud_order
    username: your_username
    password: your_password
```

### 4. 启动应用
使用Gradle进行打包和运行
或使用Idea运行

访问 http://localhost:8080 即可看到Web界面。
访问 http://localhost:8080/swagger-ui/index.html 即可看到API文档。

## API接口

### 订单管理接口
- `GET /order/list` - 获取订单列表
  - 支持的查询参数：
    - `page`: 页码（从1开始）
    - `size`: 每页大小
    - `shopName`: 店铺名称（模糊搜索）
    - `status`: 订单状态
    - `isPayed`: 支付状态
    - `minTotal`: 最小金额（单位：分）
    - `maxTotal`: 最大金额（单位：分）
    - `startTime`: 开始时间
    - `endTime`: 结束时间
    - `sortField`: 排序字段
    - `sortDirection`: 排序方向（asc/desc）

### 统计接口
- `GET /product/sales` - 获取商品销量排行
- `GET /shop/sales` - 获取店铺销售统计

## 性能测试

### 测试场景
1. 订单列表查询性能
   - 基础查询（分页）
   - 条件过滤（状态、时间范围等）
   - 模糊搜索（店铺名称）
   - 排序

2. 统计查询性能
   - 商品销量统计
   - 店铺销售额统计

## 开发计划

- [x] 基础订单管理功能
- [x] 多维度筛选
- [x] 排序功能
- [ ] 更多统计维度
- [ ] 导出功能
- [ ] 性能优化