import json
import pymysql
from faker import Faker
import random
from datetime import datetime, timedelta
from tqdm import tqdm
import threading
from queue import Queue
import time
import os

# 配置数据库连接
DB_CONFIG = {
    'host': '127.0.0.1',
    'user': 'root',
    'password': 'root',
    'db': 'mall4cloud_order',
    'charset': 'utf8mb4'
}

# 初始化Faker
fake = Faker(['zh_CN'])

# 加载shop_products.json数据
with open('data/shop_products.json', 'r', encoding='utf-8') as f:
    shop_products_data = json.load(f)

# 加载地址数据
with open('data/level.json', 'r', encoding='utf-8') as f:
    address_data = json.load(f)

def get_random_address():
    """获取随机地址"""
    try:
        # 随机选择一个省份
        province = random.choice(address_data)
        if not province.get('children'):
            return None
            
        # 判断是否是直辖市（通过检查children的第一个元素是否有city字段）
        first_child = province['children'][0]
        is_municipality = 'city' in first_child and 'children' not in first_child
        
        if is_municipality:
            # 直辖市：省份和城市相同，区域直接从children中选择
            area = random.choice(province['children'])
            return {
                'province_id': int(province['code']),
                'province_name': province['name'],
                'city_id': int(province['code']),  # 使用省份code作为城市code
                'city_name': province['name'],     # 使用省份名称作为城市名称
                'area_id': int(area['code']),
                'area_name': area['name']
            }
        else:
            # 普通省份：三级结构
            city = random.choice(province['children'])
            if not city.get('children'):
                return None
                
            area = random.choice(city['children'])
            return {
                'province_id': int(province['code']),
                'province_name': province['name'],
                'city_id': int(city['code']),
                'city_name': city['name'],
                'area_id': int(area['code']),
                'area_name': area['name']
            }
    except Exception as e:
        print(f"Error in get_random_address: {e}")
        return None

def get_random_shop_and_products():
    """获取随机店铺和其对应的商品"""
    # 随机选择一个类别
    category = random.choice(list(shop_products_data.keys()))
    # 从该类别中随机选择一个店铺
    shop_name = random.choice(list(shop_products_data[category].keys()))
    # 获取该店铺的所有商品
    products = shop_products_data[category][shop_name]
    # 随机选择1-3个商品
    selected_products = random.sample(products, random.randint(1, min(3, len(products))))
    
    shop_id = abs(hash(shop_name)) % (10 ** 8)  # 使用店铺名称的hash作为shop_id
    
    return {
        'shop_id': shop_id,
        'name': shop_name,
        'category': category,
        'products': selected_products
    }

class OrderGenerator:
    def __init__(self):
        self.order_addr_id_counter = 0
        self.order_id_counter = int(time.time() * 1000000)  # 使用时间戳作为起始值
        self.order_item_id_counter = 0
        self.lock = threading.Lock()
    
    def get_next_order_addr_id(self):
        with self.lock:
            self.order_addr_id_counter += 1
            return self.order_addr_id_counter
    
    def get_next_order_id(self):
        with self.lock:
            self.order_id_counter += 1
            return self.order_id_counter
    
    def get_next_order_item_id(self):
        with self.lock:
            self.order_item_id_counter += 1
            return self.order_item_id_counter

def generate_order_addr(generator, user_id):
    """生成订单地址数据"""
    order_addr_id = generator.get_next_order_addr_id()
    
    # 获取随机地址，如果失败则使用faker生成
    address = get_random_address()
    if address is None:
        return {
            'order_addr_id': order_addr_id,
            'user_id': user_id,
            'consignee': fake.name(),
            'province_id': random.randint(1, 34),
            'province': fake.province(),
            'city_id': random.randint(1, 400),
            'city': fake.city(),
            'area_id': random.randint(1, 3000),
            'area': fake.district(),
            'addr': fake.street_address(),
            'post_code': fake.postcode(),
            'mobile': fake.phone_number(),
            'lng': float(fake.longitude()),
            'lat': float(fake.latitude())
        }
    
    return {
        'order_addr_id': order_addr_id,
        'user_id': user_id,
        'consignee': fake.name(),
        'province_id': address['province_id'],
        'province': address['province_name'],
        'city_id': address['city_id'],
        'city': address['city_name'],
        'area_id': address['area_id'],
        'area': address['area_name'],
        'addr': fake.street_address(),
        'post_code': fake.postcode(),
        'mobile': fake.phone_number(),
        'lng': float(fake.longitude()),
        'lat': float(fake.latitude())
    }

def generate_order(order_addr, generator):
    """生成订单数据"""
    now = datetime.now()
    shop_data = get_random_shop_and_products()
    status = random.randint(1, 6)
    
    # 根据状态生成对应的时间
    create_time = now - timedelta(days=random.randint(1, 90))
    pay_time = create_time + timedelta(minutes=random.randint(1, 60)) if status >= 2 else None
    delivery_time = pay_time + timedelta(hours=random.randint(1, 48)) if status >= 3 else None
    finally_time = delivery_time + timedelta(days=random.randint(1, 7)) if status == 5 else None
    
    order_id = generator.get_next_order_id()
    
    order_data = {
        'order_id': order_id,
        'shop_id': shop_data['shop_id'],
        'user_id': order_addr['user_id'],
        'delivery_type': random.choice([1, 2, 3]),
        'shop_name': shop_data['name'],
        'status': status,
        'all_count': len(shop_data['products']),
        'create_time': create_time,
        'pay_time': pay_time,
        'delivery_time': delivery_time,
        'finally_time': finally_time,
        'is_payed': 1 if status >= 2 else 0,
        'version': 1,
        'order_addr_id': order_addr['order_addr_id'],
        'total': 0  # 初始化总金额，稍后更新
    }
    
    # 生成订单项
    order_items = generate_order_items(order_data, shop_data, generator)
    
    # 更新订单总金额
    order_data['total'] = sum(item['spu_total_amount'] for item in order_items)
    
    return order_data, order_items

def generate_order_items(order, shop_data, generator):
    """生成订单项数据"""
    items = []
    
    for product in shop_data['products']:
        # 从价格区间中选择一个价格，先转换为整数处理
        min_price = int(product['price_range'][0])
        max_price = int(product['price_range'][1])
        price = random.randint(min_price, max_price)
        item_count = random.randint(1, 3)
        
        # 生成SKU名称（添加规格信息）
        sku_specs = []
        if '手机' in product['name']:
            sku_specs.extend([random.choice(['8GB+128GB', '8GB+256GB', '12GB+256GB', '12GB+512GB'])])
        elif '笔记本' in product['name']:
            sku_specs.extend([random.choice(['i5+16GB+512GB', 'i7+16GB+1TB', 'i9+32GB+1TB'])])
        elif '服饰' in shop_data['category'] or '鞋' in shop_data['category']:
            sku_specs.extend([random.choice(['S', 'M', 'L', 'XL', 'XXL'])])
            sku_specs.extend([random.choice(['黑色', '白色', '灰色', '藏青色', '卡其色'])])
        
        sku_name = f"{product['name']} {' '.join(sku_specs)}" if sku_specs else product['name']
        
        # 使用商品名称的hash作为spu_id
        spu_id = abs(hash(product['name'])) % (10 ** 8)
        
        items.append({
            'order_item_id': generator.get_next_order_item_id(),
            'shop_id': order['shop_id'],
            'order_id': order['order_id'],
            'category_id': abs(hash(shop_data['category'])) % 1000 + 1,  # 使用类别名称的hash作为category_id
            'spu_id': spu_id,
            'sku_id': spu_id * 10 + random.randint(1, 5),
            'user_id': order['user_id'],
            'count': item_count,
            'spu_name': product['name'],
            'sku_name': sku_name,
            'pic': f"/images/products/{shop_data['category']}/{spu_id}.jpg",
            'price': price,
            'spu_total_amount': price * item_count
        })
    
    return items

def insert_batch_data(conn, table, columns, values):
    """批量插入数据"""
    cursor = conn.cursor()
    placeholders = ', '.join(['%s'] * len(columns))
    columns_str = ', '.join(columns)
    sql = f"INSERT INTO `{table}` ({columns_str}) VALUES ({placeholders})"
    cursor.executemany(sql, values)
    conn.commit()
    cursor.close()

def worker(queue, generator):
    """工作线程函数"""
    conn = pymysql.connect(**DB_CONFIG)
    
    try:
        while True:
            batch_size = queue.get()
            if batch_size is None:
                break
            
            order_addrs = []
            orders = []
            order_items = []
            
            # 生成一批数据
            for _ in range(batch_size):
                # 为整个订单流程生成同一个user_id
                user_id = random.randint(1, 100000)
                
                # 生成订单地址
                addr = generate_order_addr(generator, user_id)
                order_addrs.append(addr)
                
                # 生成订单和订单项
                order, items = generate_order(addr, generator)
                orders.append(order)
                order_items.extend(items)
            
            # 插入订单地址
            if order_addrs:
                insert_batch_data(conn, 'order_addr', 
                    ['order_addr_id', 'user_id', 'consignee', 'province_id', 'province', 
                     'city_id', 'city', 'area_id', 'area', 'addr', 'post_code', 
                     'mobile', 'lng', 'lat'],
                    [[addr[k] for k in addr.keys()] for addr in order_addrs])
            
            # 插入订单
            if orders:
                insert_batch_data(conn, 'order',
                    ['order_id', 'shop_id', 'user_id', 'delivery_type', 'shop_name', 
                     'status', 'all_count', 'create_time', 'pay_time', 'delivery_time', 
                     'finally_time', 'is_payed', 'version', 'order_addr_id', 'total'],
                    [[order[k] for k in order.keys()] for order in orders])
            
            # 插入订单项
            if order_items:
                insert_batch_data(conn, 'order_item',
                    ['order_item_id', 'shop_id', 'order_id', 'category_id', 'spu_id', 'sku_id', 
                     'user_id', 'count', 'spu_name', 'sku_name', 'pic', 'price', 
                     'spu_total_amount'],
                    [[item[k] for k in item.keys()] for item in order_items])
            
            queue.task_done()
            
    except Exception as e:
        print(f"Error in worker thread: {e}")
    finally:
        conn.close()

def main():
    total_orders = 10_000_000  # 1000万订单
    num_threads = 4  # 使用4个线程
    queue = Queue(maxsize=20)  # 限制队列大小，防止内存占用过大
    
    # 创建订单生成器
    generator = OrderGenerator()
    
    # 创建工作线程
    threads = []
    for _ in range(num_threads):
        t = threading.Thread(target=worker, args=(queue, generator))
        t.start()
        threads.append(t)
    
    # 生成并提交数据
    with tqdm(total=total_orders) as pbar:
        for i in range(0, total_orders, 1000):
            batch_size = min(1000, total_orders - i)
            queue.put(batch_size)
            pbar.update(batch_size)
    
    # 发送终止信号给工作线程
    for _ in range(num_threads):
        queue.put(None)
    
    # 等待所有线程完成
    for t in threads:
        t.join()

if __name__ == '__main__':
    start_time = time.time()
    main()
    end_time = time.time()
    print(f"\n数据生成完成！总用时: {end_time - start_time:.2f} 秒")
