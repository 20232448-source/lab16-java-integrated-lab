package com.javalab.management;

import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/audit", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class AuditController {
    private final JdbcTemplate jdbc;

    public AuditController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/status-history")
    public List<Map<String, Object>> statusHistory(@RequestParam(name = "limit", defaultValue = "50") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return jdbc.queryForList("SELECT h.id,h.order_id,h.old_status,h.new_status,h.changed_at,h.note,u.username " +
                "FROM order_status_history h LEFT JOIN users u ON u.id=h.changed_by " +
                "ORDER BY h.changed_at DESC LIMIT ?", safeLimit);
    }
}