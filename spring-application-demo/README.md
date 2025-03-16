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

### 2. 配置

根据实际情况，修改application.yml中的相关连接配置
