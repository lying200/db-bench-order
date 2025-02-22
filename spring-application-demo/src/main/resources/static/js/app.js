const { createApp, ref, h } = Vue;
const {
    NConfigProvider, NLayout, NLayoutHeader, NLayoutContent, NLayoutSider,
    NMenu, NButton, NCard, NDataTable, NTabs, NTabPane, NPageHeader,
    NTag, NSpace, darkTheme, useMessage, NPagination
} = naive;

const formatAmount = (amount) => {
    if (!amount) return '0.00';
    return (amount / 100).toFixed(2);
};

const formatDateTime = (datetime) => {
    if (!datetime) return '';
    return new Date(datetime).toLocaleString();
};

const createPagination = () => ({
    page: 1,
    pageSize: 10,
    itemCount: 0,
    showSizePicker: true,
    pageSizes: [10, 20, 50]
});

const App = {
    setup() {
        const theme = ref(null);
        const activeKey = ref('order-list');
        const orders = ref([]);
        const products = ref([]);
        const shops = ref([]);

        // loading状态
        const loadingStates = ref({
            orders: false,
            products: false,
            shops: false
        });

        // 分页状态
        const orderPagination = ref(createPagination());
        const productPagination = ref(createPagination());
        const shopPagination = ref(createPagination());

        // 菜单配置
        const menuOptions = [
            {
                label: '订单管理',
                key: 'order-list'
            },
            {
                label: '销售统计',
                key: 'sales-stats'
            }
        ];

        // 订单列表列配置
        const orderColumns = [
            { title: '订单号', key: 'orderId' },
            { title: '用户ID', key: 'userId' },
            {
                title: '订单金额',
                key: 'total',
                render(row) {
                    return h('span', { class: 'amount' }, `￥${formatAmount(row.total)}`);
                }
            },
            {
                title: '支付状态',
                key: 'isPayed',
                render(row) {
                    return h(NTag, {
                        type: row.isPayed ? 'success' : 'warning',
                        class: 'status-tag'
                    }, { default: () => row.isPayed ? '已支付' : '未支付' });
                }
            },
            {
                title: '订单状态',
                key: 'status',
                render(row) {
                    const statusMap = {
                        1: { type: 'info', text: '待付款' },
                        2: { type: 'warning', text: '待发货' },
                        3: { type: 'success', text: '已发货' },
                        4: { type: 'error', text: '已取消' },
                        5: { type: 'success', text: '成功' },
                        6: { type: 'error', text: '失败' }
                    };
                    const status = statusMap[row.status] || { type: 'default', text: '未知' };
                    return h(NTag, {
                        type: status.type,
                        class: 'status-tag'
                    }, { default: () => status.text });
                }
            },
            {
                title: '创建时间',
                key: 'createTime',
                render(row) {
                    return formatDateTime(row.createTime);
                }
            }
        ];

        // 商品销量列配置
        const productColumns = [
            { title: '商品ID', key: 'spuId' },
            { title: '商品名称', key: 'spuName' },
            { title: '销售数量', key: 'totalSold' },
            {
                title: '销售金额',
                key: 'totalAmount',
                render(row) {
                    return h('span', { class: 'amount' }, `￥${formatAmount(row.totalAmount)}`);
                }
            }
        ];

        // 店铺销售列配置
        const shopColumns = [
            { title: '店铺ID', key: 'shopId' },
            {
                title: '销售金额',
                key: 'totalAmount',
                render(row) {
                    return h('span', { class: 'amount' }, `￥${formatAmount(row.totalAmount)}`);
                }
            },
            { title: '订单数', key: 'orderCount' }
        ];

        // 加载订单列表
        const loadOrders = async () => {
            loadingStates.value.orders = true;
            try {
                const response = await fetch(`/order/list?page=${orderPagination.value.page}&size=${orderPagination.value.pageSize}`);
                if (!response.ok) throw new Error('获取订单列表失败');
                const data = await response.json();
                orders.value = data.content;
                orderPagination.value.itemCount = data.totalElements;
                orderPagination.value.pageCount = data.totalPages;
                orderPagination.value.pageSize = data.size;
                orderPagination.value.page = data.number + 1;
            } catch (error) {
                window.$message.error(error.message);
            } finally {
                loadingStates.value.orders = false;
            }
        };

        // 加载商品销量排行
        const loadProducts = async () => {
            loadingStates.value.products = true;
            try {
                const response = await fetch(`/order/stats/products?page=${productPagination.value.page}&size=${productPagination.value.pageSize}`);
                if (!response.ok) throw new Error('获取商品销量排行失败');
                const data = await response.json();
                products.value = data.content;
                productPagination.value.itemCount = data.totalElements;
                productPagination.value.pageCount = data.totalPages;
                productPagination.value.pageSize = data.size;
                productPagination.value.page = data.number + 1;
            } catch (error) {
                window.$message.error(error.message);
            } finally {
                loadingStates.value.products = false;
            }
        };

        // 加载店铺销售统计
        const loadShops = async () => {
            loadingStates.value.shops = true;
            try {
                const response = await fetch(`/order/stats/shops?page=${shopPagination.value.page}&size=${shopPagination.value.pageSize}`);
                if (!response.ok) throw new Error('获取店铺销售统计失败');
                const data = await response.json();
                shops.value = data.content;
                shopPagination.value.itemCount = data.totalElements;
                shopPagination.value.pageCount = data.totalPages;
                shopPagination.value.pageSize = data.size;
                shopPagination.value.page = data.number + 1;
            } catch (error) {
                window.$message.error(error.message);
            } finally {
                loadingStates.value.shops = false;
            }
        };

        // 分页变化处理
        const handleOrderPageChange = (page) => {
            orderPagination.value.page = page;
            loadOrders();
        };

        const handleProductPageChange = (page) => {
            productPagination.value.page = page;
            loadProducts();
        };

        const handleShopPageChange = (page) => {
            shopPagination.value.page = page;
            loadShops();
        };

        // 每页条数变化处理
        const handleOrderPageSizeChange = (pageSize) => {
            orderPagination.value.pageSize = pageSize;
            orderPagination.value.page = 1;
            loadOrders();
        };

        const handleProductPageSizeChange = (pageSize) => {
            productPagination.value.pageSize = pageSize;
            productPagination.value.page = 1;
            loadProducts();
        };

        const handleShopPageSizeChange = (pageSize) => {
            shopPagination.value.pageSize = pageSize;
            shopPagination.value.page = 1;
            loadShops();
        };

        // 菜单切换处理
        const handleMenuUpdate = (key) => {
            activeKey.value = key;
            if (key === 'order-list') {
                loadOrders();
            } else if (key === 'sales-stats') {
                loadProducts();
                loadShops();
            }
        };

        // 刷新订单列表
        const refreshOrders = () => {
            loadOrders();
        };

        // 初始加载
        loadOrders();

        return {
            theme,
            activeKey,
            menuOptions,
            orderColumns,
            productColumns,
            shopColumns,
            orders,
            products,
            shops,
            loadingStates,
            orderPagination,
            productPagination,
            shopPagination,
            handleMenuUpdate,
            handleOrderPageChange,
            handleProductPageChange,
            handleShopPageChange,
            handleOrderPageSizeChange,
            handleProductPageSizeChange,
            handleShopPageSizeChange,
            refreshOrders
        };
    }
};

// 创建Vue应用
const app = createApp(App);

// 注册Naive UI组件
app.use(naive);

const message = naive.createDiscreteApi(['message']).message;
window.$message = message;

// 挂载应用
app.mount('#app');
