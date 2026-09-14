const rows = document.querySelector('#productRows');
const search = document.querySelector('#search');
let products = [];
const money = value => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value);
const viewSections = Array.from(document.querySelectorAll('.view-section'));
const orderActionsByStatus = {
    PENDING: [
        { label: 'Xử lý', nextStatus: 'PROCESSING' },
        { label: 'Huỷ', nextStatus: 'CANCELLED' }
    ],
    PROCESSING: [
        { label: 'Sẵn sàng', nextStatus: 'READY' },
        { label: 'Huỷ', nextStatus: 'CANCELLED' }
    ],
    READY: [
        { label: 'Vận chuyển', nextStatus: 'SHIPPING' },
        { label: 'Huỷ', nextStatus: 'CANCELLED' }
    ],
    SHIPPING: [
        { label: 'Hoàn tất', nextStatus: 'COMPLETED' }
    ]
};

function render() {
    const query = search.value.trim().toLowerCase();
    const visible = products.filter(product => `${product.name} ${product.sku}`.toLowerCase().includes(query));
    document.querySelector('#productCount').textContent = `${visible.length} sản phẩm`;
    rows.innerHTML = visible.length ? visible.map(product => `<tr><td>${product.name}</td><td><span class="order-id">${product.sku}</span></td><td>${money(product.price)}</td><td class="${product.stock < 10 ? 'stock-low' : 'stock-good'}">${product.stock} chiếc</td><td><span class="pill ${product.active ? 'ready' : 'pending'}">${product.active ? 'ĐANG BÁN' : 'TẠM DỪNG'}</span></td><td>•••</td></tr>`).join('') : '<tr><td colspan="6" class="loading">Không tìm thấy sản phẩm</td></tr>';
}
async function load() {
    const [productsResponse, dashboardResponse, ordersResponse] = await Promise.all([fetch('/api/products'), fetch('/api/dashboard'), fetch('/api/orders')]);
    products = await productsResponse.json();
    const dashboard = await dashboardResponse.json();
    const orderPage = await ordersResponse.json();
    const orders = orderPage.items || [];
    document.querySelector('#revenueMetric').textContent = money(dashboard.revenue);
    document.querySelector('#productMetric').textContent = dashboard.products;
    document.querySelector('#pendingMetric').textContent = dashboard.pendingOrders;
    document.querySelector('#completedMetric').textContent = dashboard.completedOrders;
    const statusClass = { PENDING: 'pending', PROCESSING: 'processing', READY: 'ready', SHIPPING: 'processing', COMPLETED: 'ready', CANCELLED: 'pending' };
    const recentOrders = document.querySelector('#recentOrders');
    recentOrders.innerHTML = orders.length ? orders.slice(0, 5).map(order => {
        const actions = orderActionsByStatus[order.status] || [];
        const actionButtons = actions.length
            ? actions.map(action => `<button type="button" class="order-action ${action.nextStatus === 'CANCELLED' ? 'danger' : 'primary'}" data-order-id="${order.id}" data-order-version="${order.version}" data-next-status="${action.nextStatus}">${action.label}</button>`).join('')
            : '<span class="pill ready">Kết thúc</span>';
        return `<div class="order-row"><span class="order-id">#ORD-${String(order.id).padStart(4, '0')}</span><span>${order.customerName}</span><strong>${money(order.totalAmount)}</strong><span class="pill ${statusClass[order.status] || 'pending'}">${order.status}</span><small>${new Date(order.createdAt).toLocaleString('vi-VN')}</small><div class="order-actions">${actionButtons}</div></div>`;
    }).join('') : '<div class="loading">Chưa có đơn hàng</div>';

    recentOrders.querySelectorAll('.order-action').forEach(button => {
        button.addEventListener('click', async () => {
            const orderId = Number(button.dataset.orderId);
            const version = Number(button.dataset.orderVersion);
            const nextStatus = button.dataset.nextStatus;
            button.disabled = true;
            button.textContent = 'Đang cập nhật...';
            try {
                const response = await fetch(`/api/orders/${orderId}/status`, {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ status: nextStatus, version })
                });
                if (!response.ok) {
                    throw new Error('Không thể cập nhật trạng thái đơn hàng.');
                }
                await load();
            } catch (error) {
                button.disabled = false;
                button.textContent = button.dataset.label || button.textContent;
                alert(error.message || 'Có lỗi xảy ra khi cập nhật đơn hàng.');
            }
        });
    });

    render();
}
search.addEventListener('input', render);
document.querySelector('#refreshButton').addEventListener('click', load);

const navItems = document.querySelectorAll('.nav-item');
const routeMap = {
    '/overview': 'overview',
    '/orders': 'orders',
    '/products': 'products'
};

function applyRouteState() {
    const path = window.location.pathname.replace(/\/$/, '') || '/overview';
    navItems.forEach(item => {
        const isActive = item.getAttribute('href') === path;
        item.classList.toggle('active', isActive);
    });

    const targetId = routeMap[path];
    const target = targetId ? document.getElementById(targetId) : null;

    viewSections.forEach(section => {
        const isVisible = section.id === targetId;
        section.classList.toggle('hidden', !isVisible);
    });

    if (target) {
        requestAnimationFrame(() => {
            target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });
    }
}

navItems.forEach(item => {
    item.addEventListener('click', event => {
        event.preventDefault();
        const nextPath = item.getAttribute('href');
        if (nextPath) {
            history.pushState(null, '', nextPath);
            applyRouteState();
        }
    });
});

window.addEventListener('load', applyRouteState);
window.addEventListener('popstate', applyRouteState);

const modal = document.querySelector('#productModal');
document.querySelector('#newProductButton').addEventListener('click', () => modal.classList.remove('hidden'));
document.querySelector('#closeModal').addEventListener('click', () => modal.classList.add('hidden'));
modal.addEventListener('click', event => { if (event.target === modal) modal.classList.add('hidden'); });
document.querySelector('#productForm').addEventListener('submit', async event => {
    event.preventDefault();
    const form = new FormData(event.target);
    const response = await fetch('/api/products', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ sku: form.get('sku'), name: form.get('name'), price: Number(form.get('price')), stock: Number(form.get('stock')) }) });
    const message = document.querySelector('#formMessage');
    if (!response.ok) { message.textContent = 'Không thể lưu sản phẩm. Kiểm tra SKU.'; return; }
    event.target.reset(); modal.classList.add('hidden'); message.textContent = ''; await load();
});
load().catch(() => { rows.innerHTML = '<tr><td colspan="6" class="loading">Không thể kết nối máy chủ</td></tr>'; });
