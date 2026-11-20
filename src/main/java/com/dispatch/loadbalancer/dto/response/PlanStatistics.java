package com.dispatch.loadbalancer.dto.response;

import com.dispatch.loadbalancer.model.Priority;

import java.util.List;
import java.util.Map;

public record PlanStatistics(int totalOrders, int assignedOrders, int unassignedOrders,
                             double assignmentRatePercent, double assignedWeight, double fleetCapacity,
                             double fleetUtilizationPercent, double totalDistanceKm,
                             int vehiclesUsed, int vehiclesIdle,
                             Map<Priority, Long> unassignedByPriority,
                             List<VehicleStatistics> vehicles) {
}
