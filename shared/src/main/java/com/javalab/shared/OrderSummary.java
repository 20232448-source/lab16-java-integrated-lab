package com.javalab.shared;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummary(long id, long customerId, String customerName, BigDecimal totalAmount,
                           OrderStatus status, int version, Instant createdAt) {}
