package com.dispatch.loadbalancer.strategy;

import com.dispatch.loadbalancer.model.DeliveryOrder;
import com.dispatch.loadbalancer.model.Vehicle;
import com.dispatch.loadbalancer.util.HaversineCalculator;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Helpers shared by all strategies so the ordering rules and distance maths live in one place. */
final class StrategySupport {

    static final double EPS = 1e-9;

    /** HIGH -> LOW, then heavier first, then id so results are deterministic. */
    static final Comparator<DeliveryOrder> ORDER_SEQUENCE = Comparator
            .comparing(DeliveryOrder::getPriority)
            .thenComparing(DeliveryOrder::getPackageWeight, Comparator.reverseOrder())
            .thenComparing(DeliveryOrder::getOrderId);

    private StrategySupport() {
    }

    static List<VehicleState> statesSortedById(List<Vehicle> vehicles) {
        return vehicles.stream()
                .sorted(Comparator.comparing(Vehicle::getVehicleId))
                .map(VehicleState::new)
                .toList();
    }

    static double largestCapacity(List<Vehicle> vehicles) {
        return vehicles.stream().mapToDouble(Vehicle::getCapacity).max().orElse(0);
    }

    static String unassignedReason(DeliveryOrder order, double largestCapacity) {
        if (order.getPackageWeight() > largestCapacity) {
            return String.format(Locale.ROOT,
                    "Package weight %.2f exceeds the largest vehicle capacity %.2f",
                    order.getPackageWeight(), largestCapacity);
        }
        return "No vehicle has enough remaining capacity";
    }

    /** Distance from the vehicle's start through each stop in order (open route, no return leg). */
    static double pathDistance(HaversineCalculator haversine, Vehicle vehicle, List<DeliveryOrder> route) {
        double total = 0;
        double lat = vehicle.getCurrentLatitude();
        double lon = vehicle.getCurrentLongitude();
        for (DeliveryOrder o : route) {
            total += haversine.distanceKm(lat, lon, o.getLatitude(), o.getLongitude());
            lat = o.getLatitude();
            lon = o.getLongitude();
        }
        return total;
    }

    static double totalWeight(List<DeliveryOrder> orders) {
        return orders.stream().mapToDouble(DeliveryOrder::getPackageWeight).sum();
    }
}
