package com.javalab.customer;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.sql.*;
import java.util.*;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {
    @GET
    public List<Map<String, Object>> findAvailable() throws SQLException {
        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement("SELECT id,sku,name,price,stock FROM products WHERE active=TRUE ORDER BY name");
             ResultSet rs = statement.executeQuery()) {
            List<Map<String, Object>> products = new ArrayList<>();
            while (rs.next()) products.add(Map.of("id", rs.getLong("id"), "sku", rs.getString("sku"),
                    "name", rs.getString("name"), "price", rs.getBigDecimal("price"), "stock", rs.getInt("stock")));
            return products;
        }
    }
}
