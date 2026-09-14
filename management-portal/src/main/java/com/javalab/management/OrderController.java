package com.javalab.management;

import com.javalab.shared.OrderStatus;
import com.javalab.shared.OrderSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/orders", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class OrderController {
    private final JdbcTemplate jdbc;

    public OrderController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
        public OrderPage find(@RequestParam(name = "status", required = false) OrderStatus status,
                                                  @RequestParam(name = "keyword", required = false) String keyword,
                                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                                  @RequestParam(name = "size", defaultValue = "10") int size,
                                                  @AuthenticationPrincipal UserDetails user) {
                if (page < 0 || size < 1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page or size");
                List<Object> filterValues = new ArrayList<>();
                StringBuilder where = new StringBuilder(" WHERE 1=1");
                if (status != null) { where.append(" AND o.status=?"); filterValues.add(status.name()); }
                if (keyword != null && !keyword.isBlank()) { where.append(" AND (u.full_name LIKE ? OR CAST(o.id AS CHAR) LIKE ?)"); filterValues.add("%" + keyword.trim() + "%"); filterValues.add("%" + keyword.trim() + "%"); }
                if (user != null && user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) { where.append(" AND u.username=?"); filterValues.add(user.getUsername()); }
                String from = " FROM orders o JOIN users u ON u.id=o.customer_id" + where;
                long total = jdbc.queryForObject("SELECT COUNT(*)" + from, Long.class, filterValues.toArray());
                List<Object> pageValues = new ArrayList<>(filterValues);
                pageValues.add(size);
                pageValues.add(page * size);
                List<OrderSummary> orders = jdbc.query("SELECT o.id,o.customer_id,u.full_name,o.total_amount,o.status,o.version,o.created_at" + from + " ORDER BY o.created_at DESC LIMIT ? OFFSET ?", (rs, row) -> new OrderSummary(rs.getLong("id"), rs.getLong("customer_id"), rs.getString("full_name"), rs.getBigDecimal("total_amount"), OrderStatus.valueOf(rs.getString("status")), rs.getInt("version"), rs.getTimestamp("created_at").toInstant()), pageValues.toArray());
                return new OrderPage(orders, total, page, size);
    }

    @PatchMapping("/{id}/status")
        @PreAuthorize("hasAnyRole('WAREHOUSE', 'MANAGER', 'ADMIN')")
        @Transactional
        public ResponseEntity<Void> updateStatus(@PathVariable("id") long id, @RequestBody StatusRequest request) {
                if (request == null || request.status() == null || request.version() < 0)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status and version are required");
                OrderState current = jdbc.query("SELECT status,version FROM orders WHERE id=?", (rs, row) ->
                                new OrderState(OrderStatus.valueOf(rs.getString("status")), rs.getInt("version")), id)
                                .stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
                if (!canTransition(current.status(), request.status()))
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid order transition");
                int changed = jdbc.update("UPDATE orders SET status=?, version=version+1 WHERE id=? AND version=?",
                                request.status().name(), id, request.version());
                if (changed == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "Order was changed by another user");
                jdbc.update("INSERT INTO order_status_history(order_id,old_status,new_status,note) VALUES(?,?,?,?)",
                                id, current.status().name(), request.status().name(), "Updated by management portal");
        return ResponseEntity.noContent().build();
    }

        private boolean canTransition(OrderStatus from, OrderStatus to) {
                return switch (from) {
                        case PENDING -> to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
                        case PROCESSING -> to == OrderStatus.READY || to == OrderStatus.CANCELLED;
                        case READY -> to == OrderStatus.SHIPPING || to == OrderStatus.CANCELLED;
                        case SHIPPING -> to == OrderStatus.COMPLETED;
                        case COMPLETED, CANCELLED -> false;
                };
        }

        private record OrderState(OrderStatus status, int version) {}
        public record StatusRequest(OrderStatus status, int version) {}
        public record OrderPage(List<OrderSummary> items, long total, int page, int size) {}
}
