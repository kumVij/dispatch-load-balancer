package com.dispatch.loadbalancer.strategy;

import com.dispatch.loadbalancer.model.DeliveryOrder;
import com.dispatch.loadbalancer.model.Vehicle;

import java.util.List;

/** Orders are in delivery (route) order. Distance is km from the vehicle's start through every stop. */
public record VehicleAssignment(Vehicle vehicle, List<DeliveryOrder> orders,
                                double totalLoad, double totalDistanceKm) {
}
