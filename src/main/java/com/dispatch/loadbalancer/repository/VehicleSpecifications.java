package com.dispatch.loadbalancer.repository;

import com.dispatch.loadbalancer.model.Vehicle;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class VehicleSpecifications {

    private VehicleSpecifications() {
    }

    public static Specification<Vehicle> filter(Double minCapacity, Double maxCapacity, String addressContains) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (minCapacity != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<Double>get("capacity"), minCapacity));
            }
            if (maxCapacity != null) {
                predicates.add(cb.lessThanOrEqualTo(root.<Double>get("capacity"), maxCapacity));
            }
            if (addressContains != null && !addressContains.isBlank()) {
                predicates.add(cb.like(cb.lower(root.<String>get("currentAddress")),
                        "%" + addressContains.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
