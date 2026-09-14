# Jakarta EE deployment

## Payara / GlassFish

1. Build WAR: `mvn -pl customer-portal -am clean package`.
2. Start Payara Server 6 or GlassFish 7 with Jakarta EE 10.
3. Deploy `customer-portal/target/customer-portal-1.0.0-SNAPSHOT.war`.
4. Configure the datasource `jdbc/JavaLabDS` to MySQL database `java_integrated_lab`.
5. Open `http://localhost:8080/customer-portal/`.

The application also works on WildFly with the Jakarta EE 10 runtime. MySQL connector must be installed as a server module or packaged according to the selected server's datasource setup.

## Local smoke check

After deployment, check:

- `GET /customer-portal/api/products`
- `GET /customer-portal/api/orders?customerId=1`
- `POST /customer-portal/api/orders`

Example body:

```json
{"customerId":1,"items":[{"productId":1,"quantity":1}]}
```
