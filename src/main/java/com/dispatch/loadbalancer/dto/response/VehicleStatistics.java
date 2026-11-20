package com.dispatch.loadbalancer.dto.response;

public record VehicleStatistics(String vehicleId, double capacity, double totalLoad,
                                double utilizationPercent, int stops, double distanceKm) {
}
