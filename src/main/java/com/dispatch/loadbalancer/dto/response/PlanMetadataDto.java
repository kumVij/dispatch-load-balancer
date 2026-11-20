package com.dispatch.loadbalancer.dto.response;

import java.time.Instant;

public record PlanMetadataDto(String planId, Instant createdAt, String strategy, int totalOrders,
                              int assignedOrders, int unassignedOrders, double totalDistanceKm,
                              double fleetUtilizationPercent, long computationMillis) {
}
