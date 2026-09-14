package com.javalab.management;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
public class DashboardController {
    private final JdbcTemplate jdbc;
    public DashboardController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping(value = "/api/dashboard", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Map<String, Object> dashboard() {
        Map<String, Object> result = new HashMap<>();
        result.put("products", jdbc.queryForObject("SELECT COUNT(*) FROM products WHERE active=TRUE", Integer.class));
        result.put("pendingOrders", jdbc.queryForObject("SELECT COUNT(*) FROM orders WHERE status='PENDING'", Integer.class));
        result.put("completedOrders", jdbc.queryForObject("SELECT COUNT(*) FROM orders WHERE status='COMPLETED'", Integer.class));
        result.put("revenue", jdbc.queryForObject("SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE status='COMPLETED'", java.math.BigDecimal.class));
        result.put("ordersByStatus", jdbc.query("SELECT status,COUNT(*) AS total FROM orders GROUP BY status", (rs, row) -> Map.of("status", rs.getString("status"), "total", rs.getInt("total"))));
        return result;
    }
}
