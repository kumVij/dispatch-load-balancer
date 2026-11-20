package com.dispatch.loadbalancer.util;

import com.dispatch.loadbalancer.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestValidatorTest {

    @Test
    void acceptsUniqueIds() {
        assertDoesNotThrow(() -> RequestValidator.requireUniqueIds(List.of("A", "B"), "orderId(s)"));
    }

    @Test
    void rejectsDuplicateIdsAndNamesThem() {
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> RequestValidator.requireUniqueIds(List.of("A", "B", "A"), "orderId(s)"));
        assertTrue(ex.getMessage().contains("A"));
    }
}
