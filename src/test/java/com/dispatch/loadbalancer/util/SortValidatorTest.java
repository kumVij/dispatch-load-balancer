package com.dispatch.loadbalancer.util;

import com.dispatch.loadbalancer.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortValidatorTest {

    private static final Set<String> ALLOWED = Set.of("id", "weight");

    @Test
    void allowsWhitelistedProperties() {
        assertDoesNotThrow(() -> SortValidator.requireAllowed(PageRequest.of(0, 5, Sort.by("weight")), ALLOWED));
    }

    @Test
    void rejectsOtherProperties() {
        assertThrows(InvalidRequestException.class,
                () -> SortValidator.requireAllowed(PageRequest.of(0, 5, Sort.by("bogus")), ALLOWED));
    }

    @Test
    void ignoresUnpagedRequests() {
        assertDoesNotThrow(() -> SortValidator.requireAllowed(Pageable.unpaged(), ALLOWED));
    }
}
