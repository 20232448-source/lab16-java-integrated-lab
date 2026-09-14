package com.javalab.customer;

import com.javalab.shared.OrderStatus;
import jakarta.json.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {
    @GET
    public List<Map<String, Object>> findByCustomer(@QueryParam("customerId") long customerId) throws SQLException {
        if (customerId <= 0) throw new BadRequestException("customerId is required");
        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id,created_at,total_amount,status FROM orders WHERE customer_id=? ORDER BY created_at DESC")) {
            statement.setLong(1, customerId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Map<String, Object>> result = new ArrayList<>();
                while (rs.next()) result.add(order(rs));
                return result;
            }
        }
    }

    @POST
    public Response create(InputStream input) throws SQLException {
        JsonObject request;
        try (JsonReader reader = Json.createReader(input)) {
            JsonStructure structure = reader.read();
            if (!(structure instanceof JsonObject jsonObject)) {
                throw new BadRequestException("Invalid request body");
            }
            request = jsonObject;
        } catch (JsonException | IllegalStateException error) {
            throw new BadRequestException("Invalid request body");
        }

        long customerId = request.containsKey("customerId") && !request.isNull("customerId") ? request.getJsonNumber("customerId").longValue() : -1;
        JsonArray itemsArray = request.containsKey("items") && !request.isNull("items") ? request.getJsonArray("items") : null;
        if (customerId <= 0 || itemsArray == null || itemsArray.isEmpty()) {
            throw new BadRequestException("Customer and at least one item are required");
        }

        try (Connection connection = Database.open();
             PreparedStatement customer = connection.prepareStatement("SELECT id FROM users WHERE id=? AND role='CUSTOMER' AND active=TRUE")) {
            customer.setLong(1, customerId);
            try (ResultSet rs = customer.executeQuery()) {
                if (!rs.next()) {
                    throw new BadRequestException("Only customer accounts can place orders");
                }
            }
        }

        List<Item> items = new ArrayList<>();
        for (int i = 0; i < itemsArray.size(); i++) {
            JsonValue itemValue = itemsArray.get(i);
            if (!(itemValue instanceof JsonObject itemObject)) {
                throw new BadRequestException("Invalid order item");
            }
            long productId = itemObject.containsKey("productId") && !itemObject.isNull("productId") ? itemObject.getJsonNumber("productId").longValue() : -1;
            int quantity = itemObject.containsKey("quantity") && !itemObject.isNull("quantity") ? itemObject.getInt("quantity") : -1;
            if (productId <= 0 || quantity <= 0) {
                throw new BadRequestException("Invalid order item");
            }
            items.add(new Item(productId, quantity));
        }

        try (Connection connection = Database.open()) {
            connection.setAutoCommit(false);
            try {
                long orderId;
                try (PreparedStatement order = connection.prepareStatement(
                        "INSERT INTO orders(customer_id,total_amount,status) VALUES(?,0,'PENDING')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    order.setLong(1, customerId); order.executeUpdate();
                    try (ResultSet keys = order.getGeneratedKeys()) { keys.next(); orderId = keys.getLong(1); }
                }
                BigDecimal total = BigDecimal.ZERO;
                for (Item item : items) {
                    try (PreparedStatement product = connection.prepareStatement("SELECT price,stock FROM products WHERE id=? AND active=TRUE FOR UPDATE")) {
                        product.setLong(1, item.productId());
                        try (ResultSet rs = product.executeQuery()) {
                            if (!rs.next() || rs.getInt("stock") < item.quantity()) throw new BadRequestException("Product unavailable");
                            BigDecimal price = rs.getBigDecimal("price"); total = total.add(price.multiply(BigDecimal.valueOf(item.quantity())));
                            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO order_items(order_id,product_id,quantity,unit_price) VALUES(?,?,?,?)")) {
                                insert.setLong(1, orderId); insert.setLong(2, item.productId()); insert.setInt(3, item.quantity()); insert.setBigDecimal(4, price); insert.executeUpdate();
                            }
                            try (PreparedStatement stock = connection.prepareStatement("UPDATE products SET stock=stock-? WHERE id=?")) {
                                stock.setInt(1, item.quantity()); stock.setLong(2, item.productId()); stock.executeUpdate();
                            }
                        }
                    }
                }
                try (PreparedStatement update = connection.prepareStatement("UPDATE orders SET total_amount=? WHERE id=?")) {
                    update.setBigDecimal(1, total); update.setLong(2, orderId); update.executeUpdate();
                }
                connection.commit();
                return Response.status(Response.Status.CREATED).entity(Map.of("orderId", orderId, "status", OrderStatus.PENDING)).build();
            } catch (RuntimeException | SQLException error) { connection.rollback(); throw error; }
        }
    }

    private Map<String, Object> order(ResultSet rs) throws SQLException {
        return Map.of("id", rs.getLong("id"), "createdAt", rs.getTimestamp("created_at").toInstant(),
                "totalAmount", rs.getBigDecimal("total_amount"), "status", rs.getString("status"));
    }

    public record Item(long productId, int quantity) {}
    public record CreateOrderRequest(long customerId, List<Item> items) {}
}
