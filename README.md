# db-bench-order

基于电商订单场景的简单数据库性能测试，用于测试和比较 MySQL、PostgreSQL、Doris、ClickHouse 和 Elasticsearch 等不同数据库的性能表现。

## 项目结构

```
db-bench-order/
├── data-generator/              # 数据生成模块
│   ├── data/                   # 基础数据文件
│   │   ├── level.json         # 地区层级数据
│   │   └── shop_products.json # 店铺和商品数据
│   ├── generate_order_data.py  # 数据生成脚本
│   ├── mall4cloud_order.sql   # 数据库表结构
│   ├── performance_test_queries.sql  # 测试查询语句
│   └── README.md              # 数据生成模块说明
│
├── spring-application-demo/               # Spring Boot示例应用
│   ├── src/                  # 源代码目录
│   ├── pom.xml              # Maven配置文件
│   └── README.md            # 示例应用说明
│
└── README.md               # 项目总体说明
```

## 模块说明

### 1. 数据生成器 (data-generator)
- 生成模拟电商订单数据
- 提供各类测试查询语句

### 2. Spring Boot示例 (spring-application-demo)
- 基于Spring Boot的REST API示例
- 实现常见的电商业务接口
- 包含性能测试用例

## 快速开始

请参考各个模块下的README文件，了解具体的使用方法：

- [数据生成器说明](data-generator/README.md)
- [Spring Boot示例说明](spring-application-demo/README.md)