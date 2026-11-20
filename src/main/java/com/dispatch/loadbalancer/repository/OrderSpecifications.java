package com.dispatch.loadbalancer.repository;

import com.dispatch.loadbalancer.model.DeliveryOrder;
import com.dispatch.loadbalancer.model.Priority;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<DeliveryOrder> filter(Priority priority, Double minWeight,
                                                      Double maxWeight, String addressContains) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (minWeight != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<Double>get("packageWeight"), minWeight));
            }
            if (maxWeight != null) {
                predicates.add(cb.lessThanOrEqualTo(root.<Double>get("packageWeight"), maxWeight));
            }
            if (addressContains != null && !addressContains.isBlank()) {
                predicates.add(cb.like(cb.lower(root.<String>get("address")),
                        "%" + addressContains.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
