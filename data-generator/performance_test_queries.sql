-- 性能测试查询集
-- 包含各种场景的查询用例，用于测试不同数据库的性能

-- 1. 简单查询
-- 1.1 基本的SELECT查询
-- 业务场景：查询最近7天的订单列表，用于订单管理页面展示
SELECT * FROM order_info 
WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
LIMIT 50;

-- 1.2 带索引的等值查询
-- 业务场景：查询特定用户的订单历史，用于用户中心的订单列表
SELECT * FROM order_info 
WHERE user_id = 12345
ORDER BY create_time DESC
LIMIT 20;

-- 1.3 带IN条件的查询
-- 业务场景：查询多个状态的订单，用于订单处理页面的状态筛选
SELECT * FROM order_info 
WHERE status IN (1, 2, 3, 4)
ORDER BY create_time DESC
LIMIT 50;

-- 2. 排序查询
-- 2.1 单字段排序
-- 业务场景：查询最近一个月的高价值订单，用于识别重要客户
SELECT * FROM order_info 
WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
ORDER BY total_amount DESC 
LIMIT 100;

-- 2.2 多字段排序
-- 业务场景：查询待处理订单列表，按时间和金额排序，用于订单处理优先级排序
SELECT * FROM order_info 
WHERE status = 1
ORDER BY create_time DESC, total_amount DESC 
LIMIT 50;

-- 3. 模糊搜索
-- 3.1 前缀匹配
-- 业务场景：根据收货人姓名搜索订单，用于客服查询订单
SELECT * FROM order_addr 
WHERE consignee LIKE '张%' 
LIMIT 20;

-- 3.2 包含匹配
-- 业务场景：根据收货地址搜索订单，用于物流配送区域分析
SELECT * FROM order_addr 
WHERE addr LIKE '%北京市海淀区%'
LIMIT 20;

-- 4. 聚合查询
-- 4.1 基础聚合
-- 业务场景：统计各订单状态的订单数量和金额，用于订单概览仪表板
SELECT 
    status,
    COUNT(*) as order_count,
    SUM(total_amount) as total_sales,
    AVG(total_amount) as avg_order_amount
FROM order_info 
GROUP BY status;

-- 4.2 时间维度聚合
-- 业务场景：最近30天的每日订单统计，用于销售趋势分析
SELECT 
    DATE(create_time) as order_date,
    COUNT(*) as daily_orders,
    SUM(total_amount) as daily_sales
FROM order_info 
WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(create_time)
ORDER BY order_date;

-- 4.3 多维度聚合
-- 业务场景：各省市的销售统计，用于地域销售分析报表
SELECT 
    oa.province,
    oa.city,
    COUNT(DISTINCT oi.user_id) as user_count,
    COUNT(*) as order_count,
    SUM(oi.total_amount) as total_sales
FROM order_info oi
JOIN order_addr oa ON oi.order_addr_id = oa.order_addr_id
GROUP BY oa.province, oa.city
ORDER BY total_sales DESC
LIMIT 100;

-- 5. 多表连接查询
-- 5.1 简单JOIN
-- 业务场景：查询最近7天订单的详细信息，包括收货地址，用于订单管理
SELECT 
    oi.*,
    oa.province,
    oa.city,
    oa.addr
FROM order_info oi
JOIN order_addr oa ON oi.order_addr_id = oa.order_addr_id
WHERE oi.create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
LIMIT 50;

-- 5.2 多表JOIN
-- 业务场景：查询待处理订单的详细信息，包括商品信息，用于订单处理
SELECT 
    oi.order_id,
    oi.create_time,
    oi.total_amount,
    oa.province,
    oa.city,
    oit.spu_name,
    oit.count as item_count,
    oit.price as item_price
FROM order_info oi
JOIN order_addr oa ON oi.order_addr_id = oa.order_addr_id
JOIN order_item oit ON oi.order_id = oit.order_id
WHERE oi.status = 1
ORDER BY oi.create_time DESC
LIMIT 50;

-- 6. 复杂查询
-- 6.1 子查询
-- 业务场景：查询高价值客户（订单金额≥1000且订单数≥3）的消费统计
SELECT 
    user_id,
    COUNT(*) as order_count,
    SUM(total_amount) as total_spent
FROM order_info
WHERE user_id IN (
    SELECT DISTINCT user_id 
    FROM order_info 
    WHERE total_amount >= 1000
)
GROUP BY user_id
HAVING order_count >= 3
LIMIT 100;

-- 6.2 复杂统计
-- 业务场景：各省份最近90天的销售统计报表，用于区域业务分析
SELECT 
    oa.province,
    COUNT(DISTINCT oi.user_id) as user_count,
    COUNT(DISTINCT oi.order_id) as order_count,
    SUM(oi.total_amount) as total_sales,
    AVG(oi.total_amount) as avg_order_amount,
    MIN(oi.total_amount) as min_order_amount,
    MAX(oi.total_amount) as max_order_amount,
    COUNT(DISTINCT DATE(oi.create_time)) as active_days
FROM order_info oi
JOIN order_addr oa ON oi.order_addr_id = oa.order_addr_id
WHERE oi.create_time >= DATE_SUB(NOW(), INTERVAL 90 DAY)
GROUP BY oa.province
ORDER BY total_sales DESC;

-- 6.3 窗口函数
-- 业务场景：计算用户最近30天的累计消费金额和订单序号，用于用户消费分析
SELECT 
    order_id,
    user_id,
    create_time,
    total_amount,
    SUM(total_amount) OVER (PARTITION BY user_id ORDER BY create_time) as user_cumulative_amount,
    ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY create_time) as user_order_sequence
FROM order_info
WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
LIMIT 1000;

-- 7. 报表类查询
-- 7.1 用户消费分析
-- 业务场景：分析最近180天用户的消费层级分布，用于用户价值分析
WITH user_stats AS (
    SELECT 
        user_id,
        COUNT(*) as order_count,
        SUM(total_amount) as total_spent,
        MIN(create_time) as first_order_time,
        MAX(create_time) as last_order_time
    FROM order_info
    WHERE create_time >= DATE_SUB(NOW(), INTERVAL 180 DAY)
    GROUP BY user_id
)
SELECT 
    CASE 
        WHEN total_spent >= 10000 THEN '高价值'
        WHEN total_spent >= 5000 THEN '中价值'
        ELSE '低价值'
    END as user_segment,
    COUNT(*) as user_count,
    AVG(order_count) as avg_orders_per_user,
    AVG(total_spent) as avg_spent_per_user,
    SUM(total_spent) as segment_total_spent
FROM user_stats
GROUP BY 
    CASE 
        WHEN total_spent >= 10000 THEN '高价值'
        WHEN total_spent >= 5000 THEN '中价值'
        ELSE '低价值'
    END
ORDER BY segment_total_spent DESC;

-- 7.2 商品销售分析
-- 业务场景：分析最近30天热销商品，用于商品销售排行榜
SELECT 
    oit.spu_name,
    oit.sku_name,
    COUNT(DISTINCT oi.order_id) as order_count,
    SUM(oit.count) as total_quantity,
    SUM(oit.count * oit.price) as total_sales,
    AVG(oit.price) as avg_price,
    COUNT(DISTINCT oi.user_id) as buyer_count
FROM order_item oit
JOIN order_info oi ON oit.order_id = oi.order_id
WHERE oi.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY oit.spu_name, oit.sku_name
ORDER BY total_sales DESC
LIMIT 100;

-- 7.3 地域销售分析
-- 业务场景：分析最近30天各地区的销售情况，用于区域销售报表
WITH daily_region_sales AS (
    SELECT 
        DATE(oi.create_time) as sale_date,
        oa.province,
        oa.city,
        COUNT(DISTINCT oi.order_id) as daily_orders,
        SUM(oi.total_amount) as daily_sales
    FROM order_info oi
    JOIN order_addr oa ON oi.order_addr_id = oa.order_addr_id
    WHERE oi.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
    GROUP BY DATE(oi.create_time), oa.province, oa.city
)
SELECT 
    province,
    city,
    SUM(daily_orders) as total_orders,
    SUM(daily_sales) as total_sales,
    AVG(daily_sales) as avg_daily_sales,
    MAX(daily_sales) as max_daily_sales,
    COUNT(DISTINCT sale_date) as active_days
FROM daily_region_sales
GROUP BY province, city
ORDER BY total_sales DESC
LIMIT 100;

-- 8. 性能压测查询
-- 8.1 高基数分组
-- 业务场景：计算所有用户的订单统计，用于用户消费报表导出
SELECT 
    user_id,
    COUNT(*) as order_count,
    SUM(total_amount) as total_amount
FROM order_info
GROUP BY user_id
LIMIT 10000;

-- 8.2 大结果集排序
-- 业务场景：导出最近90天的订单数据，用于数据分析
SELECT *
FROM order_info
WHERE create_time >= DATE_SUB(NOW(), INTERVAL 90 DAY)
ORDER BY create_time DESC
LIMIT 5000;

-- 8.3 复杂条件
-- 业务场景：多条件筛选订单，用于特定条件的订单查询
SELECT 
    oi.order_id,
    oi.create_time,
    oi.total_amount,
    oa.province,
    oa.city
FROM order_info oi
JOIN order_addr oa ON oi.order_addr_id = oa.order_addr_id
WHERE oi.status IN (1, 2, 3)
    AND oi.total_amount >= 1000
    AND oi.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
    AND (
        oa.province IN ('北京市', '上海市', '广东省')
        OR oi.total_amount >= 5000
    )
ORDER BY oi.create_time DESC
LIMIT 200;
