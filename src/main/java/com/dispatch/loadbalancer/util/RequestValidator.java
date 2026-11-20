package com.dispatch.loadbalancer.util;

import com.dispatch.loadbalancer.exception.InvalidRequestException;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public final class RequestValidator {

    private RequestValidator() {
    }

    /** Rejects a batch that repeats the same id more than once. */
    public static void requireUniqueIds(Collection<String> ids, String label) {
        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new LinkedHashSet<>();
        for (String id : ids) {
            if (!seen.add(id)) {
                duplicates.add(id);
            }
        }
        if (!duplicates.isEmpty()) {
            throw new InvalidRequestException("Duplicate " + label + " in request: " + duplicates);
        }
    }
}
