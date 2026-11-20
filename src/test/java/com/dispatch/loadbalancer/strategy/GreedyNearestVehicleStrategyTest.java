package com.dispatch.loadbalancer.strategy;

import com.dispatch.loadbalancer.model.DeliveryOrder;
import com.dispatch.loadbalancer.model.Priority;
import com.dispatch.loadbalancer.model.Vehicle;
import com.dispatch.loadbalancer.util.HaversineCalculator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreedyNearestVehicleStrategyTest {

    private final HaversineCalculator haversine = new HaversineCalculator();
    private final GreedyNearestVehicleStrategy strategy = new GreedyNearestVehicleStrategy(haversine);

    private static DeliveryOrder order(String id, double lat, double lon, double weight, Priority p) {
        return new DeliveryOrder(id, lat, lon, "addr-" + id, weight, p);
    }

    private static Vehicle vehicle(String id, double capacity, double lat, double lon) {
        return new Vehicle(id, capacity, lat, lon, "start-" + id);
    }

    @Test
    void highPriorityIsAssignedBeforeLowWhenCapacityIsTight() {
        var result = strategy.plan(
                List.of(order("LOW1", 0, 0, 10, Priority.LOW), order("HIGH1", 0, 0, 10, Priority.HIGH)),
                List.of(vehicle("V1", 10, 0, 0)));

        assertEquals(1, result.assignments().get(0).orders().size());
        assertEquals("HIGH1", result.assignments().get(0).orders().get(0).getOrderId());
        assertEquals("LOW1", result.unassignedOrders().get(0).order().getOrderId());
    }

    @Test
    void neverExceedsVehicleCapacity() {
        var orders = List.of(
                order("O1", 1, 1, 30, Priority.HIGH), order("O2", 1, 2, 30, Priority.MEDIUM),
                order("O3", 2, 1, 30, Priority.LOW), order("O4", 2, 2, 30, Priority.HIGH),
                order("O5", 3, 3, 30, Priority.MEDIUM));
        var vehicles = List.of(vehicle("V1", 50, 0, 0), vehicle("V2", 80, 5, 5));

        var result = strategy.plan(orders, vehicles);

        for (VehicleAssignment a : result.assignments()) {
            assertTrue(a.totalLoad() <= a.vehicle().getCapacity() + 1e-9,
                    a.vehicle().getVehicleId() + " is overloaded");
        }
    }

    @Test
    void everyOrderIsEitherAssignedOrReportedUnassigned() {
        var orders = List.of(
                order("O1", 1, 1, 60, Priority.HIGH), order("O2", 1, 2, 60, Priority.HIGH),
                order("O3", 2, 1, 60, Priority.LOW));
        var result = strategy.plan(orders, List.of(vehicle("V1", 100, 0, 0)));

        long assigned = result.assignments().stream().mapToLong(a -> a.orders().size()).sum();
        assertEquals(orders.size(), assigned + result.unassignedOrders().size());
    }

    @Test
    void overweightOrderIsUnassignedWithReason() {
        var result = strategy.plan(
                List.of(order("BIG", 0, 0, 500, Priority.HIGH)),
                List.of(vehicle("V1", 100, 0, 0)));

        assertEquals(1, result.unassignedOrders().size());
        assertTrue(result.unassignedOrders().get(0).reason().contains("exceeds"));
    }

    @Test
    void orderGoesToNearestVehicle() {
        var result = strategy.plan(
                List.of(order("O1", 0.1, 0.1, 5, Priority.MEDIUM)),
                List.of(vehicle("FAR", 100, 10, 10), vehicle("NEAR", 100, 0, 0)));

        VehicleAssignment near = result.assignments().stream()
                .filter(a -> a.vehicle().getVehicleId().equals("NEAR")).findFirst().orElseThrow();
        assertEquals(1, near.orders().size());
    }

    @Test
    void emptyOrdersProduceEmptyAssignments() {
        var result = strategy.plan(List.of(), List.of(vehicle("V1", 100, 0, 0)));

        assertTrue(result.unassignedOrders().isEmpty());
        assertEquals(0.0, result.assignments().get(0).totalDistanceKm(), 1e-9);
    }

    @Test
    void totalDistanceMatchesHaversineForSingleStop() {
        var result = strategy.plan(
                List.of(order("O1", 28.5355, 77.3910, 10, Priority.HIGH)),
                List.of(vehicle("V1", 100, 28.6139, 77.2090)));

        double expected = haversine.distanceKm(28.6139, 77.2090, 28.5355, 77.3910);
        assertEquals(expected, result.assignments().get(0).totalDistanceKm(), 1e-9);
    }

    @Test
    void twoOptNeverMakesRouteLongerThanInsertionOrder() {
        var orders = List.of(
                order("A", 0, 3, 1, Priority.HIGH), order("B", 0, 1, 1, Priority.HIGH),
                order("C", 0, 2, 1, Priority.HIGH), order("D", 0, 4, 1, Priority.HIGH));
        var result = strategy.plan(orders, List.of(vehicle("V1", 100, 0, 0)));

        // Best open route from (0,0) along a line is B -> C -> A -> D, i.e. 0 -> 4 degrees of longitude.
        double straightLine = haversine.distanceKm(0, 0, 0, 4);
        assertEquals(straightLine, result.assignments().get(0).totalDistanceKm(), 1e-6);
    }
}
