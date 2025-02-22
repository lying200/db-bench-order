# data-generator

基于电商订单场景的数据库性能测试，用于测试和比较 MySQL、PostgreSQL、Doris、ClickHouse 和 Elasticsearch 等不同数据库的性能表现。

> 🚩内容基于AI生成，可能包含错误，生成数据经验证基本可用， [performance_test_queries.sql](./performance_test_queries.sql) 可能存在错误，可自行根据实际情况修改。

## 项目简介

本项目提供了一套工具和查询集合，用于使用电商数据进行数据库性能测试。它可以生成模拟订单数据，并包含了电商应用中常见的各种查询场景。

## 主要特性

- **相对真实数据生成**
  - 包含多种状态的订单信息
  - 包含层级结构的用户地址数据
  - 包含SKU变体的商品详情
  - 包含分类的店铺信息

- **较全面的查询场景**
  - 基础的增删改查操作
  - 复杂的聚合统计
  - 多表关联查询
  - 时间序列分析
  - 地理位置统计
  - 用户行为分析

- **多种数据库测试**
  - MySQL
  - PostgreSQL
  - Apache Doris
  - ClickHouse
  - Elasticsearch

## 项目结构

```
db-bench-ecommerce/
├── data/
│   ├── level.json           # 地区层级数据
│   ├── shop_products.json   # 店铺和商品数据
├── generate_order_data.py   # 数据生成脚本
├── mall4cloud_order.sql     # 数据库表结构
├── performance_test_queries.sql  # 测试查询语句
└── README.md
```

## 快速开始

### 环境要求

- Python 3.8+
- Python依赖包：
  ```
  pymysql
  faker
  tqdm
  ```
- 数据库：
  - MySQL 8.0+

### 安装步骤

1. 克隆仓库
```bash
git clone https://github.com/lying200/db-bench-order.git
cd db-bench-order/data-generator
```

2. 安装依赖包
```bash
pip install -r requirements.txt
```

3. 在 `generate_order_data.py` 中配置数据库连接信息

### 使用方法

1. 生成测试数据
```bash
python generate_order_data.py
```
测试数据生成总用时: 16915.59 秒（4小时41分钟）

2. 在你选择的数据库中运行 `performance_test_queries.sql` 中的测试查询

## 查询类别

测试包含以下类型的查询：

1. **简单查询**
   - 基础的SELECT操作
   - 基于索引的查询
   - IN子句查询

2. **排序查询**
   - 单字段排序
   - 多字段排序

3. **搜索查询**
   - 前缀匹配
   - 模式匹配

4. **聚合查询**
   - 基础聚合
   - 基于时间的聚合
   - 多维度聚合

5. **关联查询**
   - 简单关联
   - 多表关联

6. **复杂查询**
   - 子查询
   - 窗口函数
   - 复杂条件

7. **报表查询**
   - 用户消费分析
   - 商品销售分析
   - 区域销售分析

8. **性能测试查询**
   - 高基数分组
   - 大结果集排序
   - 复杂条件筛选

## 数据规模

生成的测试数据包括：
- 订单：可配置，默认1000万条记录
- 订单项：2000-3000万条记录

## 业务场景

测试查询涵盖了电商系统中的常见业务场景：
- 订单管理和查询
- 商品销售分析
- 用户行为分析
- 地域销售分析
- 实时统计报表
- 历史数据分析

## 其他

- 数据结构参考了真实电商系统
- 地址数据基于中国行政区划
- 查询模式来自常见电商场景
- 表结构来源： [mall4cloud](https://github.com/gz-yami/mall4cloud/blob/master/db/mall4cloud_order.sql)
