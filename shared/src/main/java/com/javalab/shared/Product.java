package com.javalab.shared;

import java.math.BigDecimal;

public record Product(long id, String sku, String name, BigDecimal price, int stock, boolean active) {}
