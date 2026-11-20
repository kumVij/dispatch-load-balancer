package com.dispatch.loadbalancer.util;

import com.dispatch.loadbalancer.exception.InvalidRequestException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;
import java.util.TreeSet;

public final class SortValidator {

    private SortValidator() {
    }

    /** Turns a bad {@code sort=} parameter into a clean 400 instead of a persistence error. */
    public static void requireAllowed(Pageable pageable, Set<String> allowedProperties) {
        if (pageable.isUnpaged()) {
            return;
        }
        for (Sort.Order order : pageable.getSort()) {
            if (!allowedProperties.contains(order.getProperty())) {
                throw new InvalidRequestException("Cannot sort by '" + order.getProperty()
                        + "'. Allowed: " + new TreeSet<>(allowedProperties));
            }
        }
    }
}
