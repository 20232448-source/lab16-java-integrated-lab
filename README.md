# Lab 16 - Java Integrated Order System

Bai lab gom ba ung dung dung chung MySQL:

- `customer-portal`: Jakarta EE 10, REST API cho khach hang tao va xem don hang.
- `warehouse-desktop`: Java Swing + JDBC, xu ly don va cap nhat ton kho.
- `management-portal`: Spring Boot, quan ly san pham va phe duyet giao hang.
- `shared`: enum va DTO dung chung.

## Chay nhanh

1. Chay `database/schema.sql` tren MySQL.
2. Sua thong tin ket noi trong `customer-portal/src/main/resources/META-INF/microprofile-config.properties`, `management-portal/src/main/resources/application.properties` va `warehouse-desktop`.
3. Build: `mvn clean package`.
4. Deploy WAR len Jakarta EE server (Payara/GlassFish/WildFly), chay Spring Boot bang `mvn -pl management-portal spring-boot:run`, hoac chay JAR Swing.
5. Chay benchmark API: `powershell -ExecutionPolicy Bypass -File tools/benchmark-api.ps1`.

## Cac module da co giao dien

- Customer Portal: deploy WAR va mo `/customer-portal/` de xem san pham, them gio hang, tao don va xem lich su.
- Warehouse Desktop: `java -jar warehouse-desktop/target/warehouse-desktop-1.0.0-SNAPSHOT.jar`; co loc trang thai, xem chi tiet, transaction va chuyen PENDING -> PROCESSING -> READY -> SHIPPING -> COMPLETED.
- Huong dan deploy Jakarta EE chi tiet nam trong `deploy/README.md`.

Tai khoan mau deu co mat khau `123456`. Trong moi truong that, password phai duoc hash bang BCrypt.
