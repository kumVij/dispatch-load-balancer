package com.dispatch.loadbalancer.dto.response;

public record VehicleDto(String vehicleId, double capacity, double currentLatitude,
                         double currentLongitude, String currentAddress) {
}
