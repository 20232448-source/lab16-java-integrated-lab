CREATE DATABASE IF NOT EXISTS java_integrated_lab CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE java_integrated_lab;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('CUSTOMER','WAREHOUSE','MANAGER','ADMIN') NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status ENUM('PENDING','PROCESSING','READY','SHIPPING','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) AS (quantity * unit_price) STORED,
    CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    changed_by BIGINT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note VARCHAR(255),
    CONSTRAINT fk_history_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_history_user FOREIGN KEY (changed_by) REFERENCES users(id)
);

CREATE INDEX idx_orders_status_created ON orders(status, created_at);
CREATE INDEX idx_orders_customer_created ON orders(customer_id, created_at);
CREATE INDEX idx_order_history_order_changed ON order_status_history(order_id, changed_at);
CREATE INDEX idx_products_active_stock ON products(active, stock);

INSERT IGNORE INTO users(username,password_hash,full_name,role) VALUES
('customer01','123456','Nguyễn Văn Khách','CUSTOMER'),
('warehouse01','123456','Trần Văn Kho','WAREHOUSE'),
('manager01','123456','Lê Văn Quản Lý','MANAGER'),
('admin01','123456','Quản trị viên','ADMIN');
INSERT IGNORE INTO products(sku,name,price,stock) VALUES
('SP001','Bàn phím cơ',850000,20),('SP002','Chuột không dây',350000,50),('SP003','Tai nghe Bluetooth',650000,15);
