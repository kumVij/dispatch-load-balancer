package com.dispatch.loadbalancer.dto.response;

import java.util.List;

/** One vehicle's slice of the dispatch plan. {@code assignedOrders} is in delivery (route) order. */
public record AssignedVehicleDto(String vehicleId, double capacity, double totalLoad,
                                 String totalDistance, double totalDistanceKm,
                                 List<OrderDto> assignedOrders) {
}
