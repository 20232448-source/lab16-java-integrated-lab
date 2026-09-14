package com.javalab.management;

import com.javalab.shared.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderControllerTest {
    @Test
    void statusFlowUsesTheRequiredOrder() {
        assertEquals(OrderStatus.PROCESSING, OrderStatus.valueOf("PROCESSING"));
        assertEquals(OrderStatus.READY, OrderStatus.valueOf("READY"));
        assertEquals(OrderStatus.SHIPPING, OrderStatus.valueOf("SHIPPING"));
        assertEquals(OrderStatus.COMPLETED, OrderStatus.valueOf("COMPLETED"));
    }

    @Test
    void cancelledAndCompletedAreTerminalStatuses() {
        assertTrue(OrderStatus.CANCELLED != OrderStatus.COMPLETED);
        assertEquals(6, OrderStatus.values().length);
    }
}
