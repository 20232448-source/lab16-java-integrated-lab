# Lab 16 - Checklist nop

## Da hoan thien trong source

- [x] Maven multi-module: `shared`, `customer-portal`, `warehouse-desktop`, `management-portal`.
- [x] MySQL schema chung: users, products, orders, order_items, order_status_history.
- [x] Customer Portal REST: xem san pham, tao don trong transaction, tru ton kho va khoa dong san pham.
- [x] Customer Portal UI: catalog, gio hang, tao don va xem lich su don.
- [x] Warehouse Desktop: tai don, chuyen PENDING -> PROCESSING -> READY.
- [x] Warehouse Desktop day du: loc, xem chi tiet, SHIPPING/COMPLETED, transaction va optimistic locking.
- [x] Management Portal Spring Boot: dashboard, catalog, order API va giao dien quan tri.
- [x] Dang nhap va phan quyen demo: CUSTOMER, WAREHOUSE, MANAGER, ADMIN.
- [x] Kiem tra transition trang thai va optimistic locking bang cot version.
- [x] Luu lich su thay doi trang thai.
- [x] Tim kiem, loc theo status, phan trang va gioi han du lieu theo role.
- [x] Audit API xem lich su thay doi cho MANAGER/ADMIN.
- [x] Index truy van va script `database/benchmark.sql` cho bai hieu nang.
- [x] Benchmark API thuc te bang `tools/benchmark-api.ps1`.
- [x] WAR co `WEB-INF/web.xml` va huong dan deploy Payara/GlassFish/WildFly trong `deploy/README.md`.
- [x] UTF-8 cho tieng Viet va giao dien responsive.

## Cach demo

1. Bat MySQL XAMPP o cong 3306.
2. Import `database/schema.sql` bang charset `utf8mb4`.
3. Chay `mvn clean package`.
4. Chay management portal: `mvn -pl management-portal spring-boot:run`.
5. Mo `http://localhost:8080/` va dang nhap `manager01 / 123456`.
6. Dung tai khoan `warehouse01 / 123456` de demo quyen kho.

## Phan can tu quay/nop

- Video thao tac: quay dang nhap, xem catalog, them san pham, xem dashboard va chuyen trang thai don.
- Bao cao: chen anh giao dien, so do kien truc, schema database va bang ket qua test.
- File nop: source zip, `database/schema.sql`, README/checklist va video.

Tai khoan demo chi phuc vu lab. Moi truong that phai dung BCrypt/Argon2 va bien moi truong cho mat khau.
