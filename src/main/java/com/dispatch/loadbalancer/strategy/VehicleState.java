package com.dispatch.loadbalancer.strategy;

import com.dispatch.loadbalancer.model.DeliveryOrder;
import com.dispatch.loadbalancer.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

/** Mutable working state for one vehicle while a strategy allocates orders. */
final class VehicleState {

    final Vehicle vehicle;
    final List<DeliveryOrder> orders = new ArrayList<>();
    double remaining;
    double lat;
    double lon;

    VehicleState(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.remaining = vehicle.getCapacity();
        this.lat = vehicle.getCurrentLatitude();
        this.lon = vehicle.getCurrentLongitude();
    }

    void add(DeliveryOrder order) {
        orders.add(order);
        remaining -= order.getPackageWeight();
        lat = order.getLatitude();
        lon = order.getLongitude();
    }
}
