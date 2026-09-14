USE java_integrated_lab;

-- Bài 11: chạy trước và sau khi có index để so sánh EXPLAIN.
EXPLAIN SELECT id, customer_id, total_amount, status, created_at
FROM orders WHERE status = 'PENDING' ORDER BY created_at DESC LIMIT 10;

EXPLAIN SELECT id, customer_id, total_amount, status, created_at
FROM orders WHERE customer_id = 1 ORDER BY created_at DESC LIMIT 10;

SHOW INDEX FROM orders;
SHOW INDEX FROM order_status_history;