const { createApp, ref, h, reactive } = Vue;
const {
    NConfigProvider, NLayout, NLayoutHeader, NLayoutContent, NLayoutSider,
    NMenu, NButton, NCard, NDataTable, NTabs, NTabPane, NPageHeader,
    NTag, NSpace, darkTheme, useMessage, NPagination, NForm, NFormItem,
    NInput, NSelect, NInputNumber, NDatePicker, NGrid, NGridItem, NInputGroup,
    zhCN, dateZhCN
} = naive;

const formatAmount = (amount) => {
    if (!amount) return '0.00';
    return (amount / 100).toFixed(2);
};

const formatDateTime = (datetime) => {
    if (!datetime) return '';
    const date = new Date(datetime);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
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

        // 订单状态选项
        const orderStatusOptions = [
            { label: '待付款', value: 1 },
            { label: '待发货', value: 2 },
            { label: '待收货', value: 3 },
            { label: '成功', value: 5 },
            { label: '失败', value: 6 }
        ];

        // 支付状态选项
        const payStatusOptions = [
            { label: '已支付', value: true },
            { label: '未支付', value: false }
        ];

        // 排序字段选项
        const sortFieldOptions = [
            { label: '创建时间', value: 'createTime' },
            { label: '订单金额', value: 'total' }
        ];

        // 排序方向选项
        const sortDirectionOptions = [
            { label: '降序', value: 'desc' },
            { label: '升序', value: 'asc' }
        ];

        // 订单筛选条件
        const orderFilter = reactive({
            shopName: '',
            status: null,
            isPayed: null,
            minTotal: null,
            maxTotal: null,
            timeRange: null,
            sortField: 'createTime',
            sortDirection: 'desc'
        });

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
            { title: '店铺名称', key: 'shopName' },
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

        // 重置筛选条件
        const resetOrderFilter = () => {
            orderFilter.shopName = '';
            orderFilter.status = null;
            orderFilter.isPayed = null;
            orderFilter.minTotal = null;
            orderFilter.maxTotal = null;
            orderFilter.timeRange = null;
            orderFilter.sortField = 'createTime';
            orderFilter.sortDirection = 'desc';
            refreshOrders();
        };

        // 刷新订单列表
        const refreshOrders = async () => {
            loadingStates.value.orders = true;
            try {
                const params = new URLSearchParams({
                    page: orderPagination.value.page,
                    size: orderPagination.value.pageSize,
                    sortField: orderFilter.sortField,
                    sortDirection: orderFilter.sortDirection
                });

                if (orderFilter.shopName) {
                    params.append('shopName', orderFilter.shopName);
                }
                if (orderFilter.status !== null) {
                    params.append('status', orderFilter.status);
                }
                if (orderFilter.isPayed !== null) {
                    params.append('isPayed', orderFilter.isPayed);
                }
                if (orderFilter.minTotal !== null) {
                    params.append('minTotal', orderFilter.minTotal * 100); // 转换为分
                }
                if (orderFilter.maxTotal !== null) {
                    params.append('maxTotal', orderFilter.maxTotal * 100); // 转换为分
                }
                if (orderFilter.timeRange) {
                    const [startTime, endTime] = orderFilter.timeRange;
                    params.append('startTime', formatDateTime(startTime));
                    params.append('endTime', formatDateTime(endTime));
                }

                const response = await fetch(`/order/list?${params.toString()}`);
                const data = await response.json();
                orders.value = data.content;
                orderPagination.value.itemCount = data.totalElements;
            } catch (error) {
                console.error('Failed to fetch orders:', error);
            } finally {
                loadingStates.value.orders = false;
            }
        };

        // 处理菜单切换
        const handleMenuUpdate = (key) => {
            activeKey.value = key;
            if (key === 'order-list') {
                refreshOrders();
            } else if (key === 'sales-stats') {
                refreshProducts();
                refreshShops();
            }
        };

        // 处理订单分页变化
        const handleOrderPageChange = (page) => {
            orderPagination.value.page = page;
            refreshOrders();
        };

        // 处理订单每页数量变化
        const handleOrderPageSizeChange = (pageSize) => {
            orderPagination.value.pageSize = pageSize;
            orderPagination.value.page = 1;
            refreshOrders();
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

        // 处理商品分页变化
        const handleProductPageChange = (page) => {
            productPagination.value.page = page;
            loadProducts();
        };

        // 处理商品每页数量变化
        const handleProductPageSizeChange = (pageSize) => {
            productPagination.value.pageSize = pageSize;
            productPagination.value.page = 1;
            loadProducts();
        };

        // 处理店铺分页变化
        const handleShopPageChange = (page) => {
            shopPagination.value.page = page;
            loadShops();
        };

        // 处理店铺每页数量变化
        const handleShopPageSizeChange = (pageSize) => {
            shopPagination.value.pageSize = pageSize;
            shopPagination.value.page = 1;
            loadShops();
        };

        // 刷新商品销量排行
        const refreshProducts = async () => {
            loadProducts();
        };

        // 刷新店铺销售统计
        const refreshShops = async () => {
            loadShops();
        };

        // 初始加载
        refreshOrders();

        return {
            theme,
            zhCN,
            dateZhCN,
            activeKey,
            menuOptions,
            orderColumns,
            orders,
            orderPagination,
            loadingStates,
            handleMenuUpdate,
            handleOrderPageChange,
            handleOrderPageSizeChange,
            refreshOrders,
            // 新增
            orderFilter,
            orderStatusOptions,
            payStatusOptions,
            sortFieldOptions,
            sortDirectionOptions,
            resetOrderFilter,
            products,
            productColumns: [
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
            ],
            shops,
            shopColumns: [
                { title: '店铺ID', key: 'shopId' },
                {
                    title: '销售金额',
                    key: 'totalAmount',
                    render(row) {
                        return h('span', { class: 'amount' }, `￥${formatAmount(row.totalAmount)}`);
                    }
                },
                { title: '订单数', key: 'orderCount' }
            ],
            productPagination,
            shopPagination,
            handleProductPageChange,
            handleProductPageSizeChange,
            handleShopPageChange,
            handleShopPageSizeChange,
            refreshProducts,
            refreshShops
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
