const customerId = document.querySelector('#customerId');
const authStatus = document.querySelector('#authStatus');
const usernameInput = document.querySelector('#username');
const passwordInput = document.querySelector('#password');
const cart = new Map();
let products = [];
let currentCustomer = null;
const STORAGE_KEY = 'customer-portal-auth';
const money = value => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value);

function saveCustomerSession(customer) {
    if (customer) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(customer));
    } else {
        localStorage.removeItem(STORAGE_KEY);
    }
}

function restoreCustomerSession() {
    try {
        const saved = localStorage.getItem(STORAGE_KEY);
        if (!saved) return null;
        const parsed = JSON.parse(saved);
        if (parsed && parsed.id && parsed.username && parsed.fullName) {
            return parsed;
        }
    } catch (error) {
        console.warn('Unable to restore customer session', error);
    }
    localStorage.removeItem(STORAGE_KEY);
    return null;
}
function setMessage(text, isError = false) {
    const message = document.querySelector('#message');
    message.textContent = text;
    message.style.color = isError ? '#c62828' : '#0b6b4f';
}
function updateAuthView() {
    const hasLogin = !!currentCustomer;
    authStatus.textContent = hasLogin ? `Đã đăng nhập: ${currentCustomer.fullName} (${currentCustomer.username})` : 'Chưa đăng nhập';
    authStatus.classList.toggle('logged-in', hasLogin);

    const loginBtn = document.querySelector('#loginBtn');
    const logoutBtn = document.querySelector('#logoutBtn');

    usernameInput.style.display = hasLogin ? 'none' : 'inline-block';
    passwordInput.style.display = hasLogin ? 'none' : 'inline-block';
    loginBtn.style.display = hasLogin ? 'none' : 'inline-block';
    logoutBtn.style.display = hasLogin ? 'inline-block' : 'none';

    usernameInput.disabled = hasLogin;
    passwordInput.disabled = hasLogin;
    loginBtn.disabled = hasLogin;
    logoutBtn.disabled = !hasLogin;
}
async function loadProducts() {
    const response = await fetch('api/products');
    products = await response.json();
    document.querySelector('#productCount').textContent = `${products.length} sản phẩm`;
    document.querySelector('#products').innerHTML = products.map(product => `<article class="product"><div class="product-name">${product.name}</div><div class="sku">${product.sku}</div><div class="price">${money(product.price)}</div><div class="stock">Còn ${product.stock} chiếc</div><button ${product.stock < 1 ? 'disabled' : ''} data-id="${product.id}">＋ Thêm vào giỏ</button></article>`).join('');
    document.querySelectorAll('.product button').forEach(button => button.addEventListener('click', () => addToCart(Number(button.dataset.id))));
}
function addToCart(id) { const product = products.find(item => item.id === id); const item = cart.get(id) || { product, quantity: 0 }; if (item.quantity < product.stock) item.quantity++; cart.set(id, item); renderCart(); }
function renderCart() { const items = [...cart.values()]; document.querySelector('#cartCount').textContent = items.reduce((sum, item) => sum + item.quantity, 0); document.querySelector('#cartItems').innerHTML = items.length ? items.map(item => `<div class="cart-item"><div>${item.product.name}<small>${item.quantity} × ${money(item.product.price)}</small></div><button class="remove" data-id="${item.product.id}">×</button></div>`).join('') : '<p class="empty">Giỏ hàng đang trống</p>'; document.querySelectorAll('.remove').forEach(button => button.addEventListener('click', () => { cart.delete(Number(button.dataset.id)); renderCart(); })); document.querySelector('#total').textContent = money(items.reduce((sum, item) => sum + item.quantity * item.product.price, 0)); }
async function loadOrders() {
    const id = Number(customerId.value);
    if (!id) {
        document.querySelector('#orders').innerHTML = '<div class="empty">Đăng nhập để xem đơn hàng của bạn</div>';
        document.querySelector('#historyHint').textContent = 'Đăng nhập để xem đơn hàng';
        return;
    }
    const response = await fetch(`api/orders?customerId=${id}`);
    const orders = await response.json();
    document.querySelector('#historyHint').textContent = `${orders.length} đơn hàng`;
    document.querySelector('#orders').innerHTML = orders.length ? orders.map(order => `<div class="order"><span>#ORD-${String(order.id).padStart(4, '0')}</span><span>${new Date(order.createdAt).toLocaleString('vi-VN')}</span><strong>${money(order.totalAmount)}</strong><span class="status">${order.status}</span></div>`).join('') : '<div class="empty">Chưa có đơn hàng</div>';
}
document.querySelector('#loginBtn').addEventListener('click', async () => {
    const username = usernameInput.value.trim();
    const password = passwordInput.value.trim();
    if (!username || !password) {
        setMessage('Vui lòng nhập tên đăng nhập và mật khẩu.', true);
        return;
    }
    const response = await fetch('api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });
    const payload = await response.json().catch(() => ({}));
    if (!response.ok) {
        setMessage(payload.message || 'Sai tài khoản hoặc mật khẩu.', true);
        return;
    }
    currentCustomer = payload;
    customerId.value = String(payload.id);
    saveCustomerSession(currentCustomer);
    setMessage(`Đăng nhập thành công. Xin chào ${payload.fullName}.`, false);
    usernameInput.value = '';
    passwordInput.value = '';
    updateAuthView();
    await loadOrders();
});
document.querySelector('#logoutBtn').addEventListener('click', () => {
    currentCustomer = null;
    customerId.value = '';
    saveCustomerSession(null);
    setMessage('Bạn đã đăng xuất.', false);
    updateAuthView();
    document.querySelector('#orders').innerHTML = '<div class="empty">Đăng nhập để xem đơn hàng của bạn</div>';
    document.querySelector('#historyHint').textContent = 'Đăng nhập để xem đơn hàng';
});
document.querySelector('#checkout').addEventListener('click', async () => {
    const id = Number(customerId.value);
    if (!id) {
        setMessage('Vui lòng đăng nhập tài khoản khách hàng trước khi đặt hàng.', true);
        return;
    }
    const items = [...cart.values()].map(item => ({ productId: item.product.id, quantity: item.quantity }));
    if (!items.length) {
        setMessage('Hãy thêm ít nhất một sản phẩm.', true);
        return;
    }
    const response = await fetch('api/orders', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ customerId: id, items }) });
    const payload = await response.json().catch(() => ({}));
    if (!response.ok) {
        setMessage(payload.message || 'Không thể đặt hàng, hãy kiểm tra tồn kho hoặc tài khoản.', true);
        return;
    }
    setMessage(`Đặt hàng thành công. Mã đơn: #ORD-${String(payload.orderId).padStart(4, '0')}`, false);
    cart.clear();
    renderCart();
    await loadProducts();
    await loadOrders();
});
document.querySelector('#loadOrders').addEventListener('click', loadOrders);

currentCustomer = restoreCustomerSession();
if (currentCustomer) {
    customerId.value = String(currentCustomer.id);
}

updateAuthView();
loadProducts().catch(() => document.querySelector('#products').innerHTML = '<div class="loading">Không thể kết nối máy chủ</div>');
loadOrders();
