package com.javalab.management;

import com.javalab.shared.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping(value = "/api/products", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class ProductController {
    private final JdbcTemplate jdbc;

    public ProductController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public List<Product> findAll() {
        return jdbc.query("SELECT id, sku, name, price, stock, active FROM products ORDER BY id",
                (rs, row) -> new Product(rs.getLong("id"), rs.getString("sku"), rs.getString("name"),
                        rs.getBigDecimal("price"), rs.getInt("stock"), rs.getBoolean("active")));
    }

    @PostMapping
        @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public Product create(@RequestBody ProductRequest request) {
        jdbc.update("INSERT INTO products(sku,name,price,stock) VALUES(?,?,?,?)",
                request.sku(), request.name(), request.price(), request.stock());
        return jdbc.queryForObject("SELECT id, sku, name, price, stock, active FROM products WHERE sku=?",
                (rs, row) -> new Product(rs.getLong("id"), rs.getString("sku"), rs.getString("name"),
                        rs.getBigDecimal("price"), rs.getInt("stock"), rs.getBoolean("active")), request.sku());
    }

    public record ProductRequest(String sku, String name, BigDecimal price, int stock) {}
}
