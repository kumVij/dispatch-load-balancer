package com.dispatch.loadbalancer.strategy;

import com.dispatch.loadbalancer.model.DeliveryOrder;
import com.dispatch.loadbalancer.model.Priority;
import com.dispatch.loadbalancer.model.Vehicle;
import com.dispatch.loadbalancer.util.HaversineCalculator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriorityOnlyStrategyTest {

    private final PriorityOnlyStrategy strategy = new PriorityOnlyStrategy(new HaversineCalculator());

    private static DeliveryOrder order(String id, double weight, Priority p) {
        return new DeliveryOrder(id, 1, 1, "addr", weight, p);
    }

    private static Vehicle vehicle(String id, double capacity) {
        return new Vehicle(id, capacity, 0, 0, "depot");
    }

    @Test
    void exposesItsName() {
        assertEquals("priority-only", strategy.name());
    }

    @Test
    void spreadsLoadAcrossVehiclesWithMostSpareCapacity() {
        var result = strategy.plan(
                List.of(order("O1", 10, Priority.HIGH), order("O2", 10, Priority.HIGH)),
                List.of(vehicle("V1", 100), vehicle("V2", 100)));

        assertEquals(1, result.assignments().get(0).orders().size());
        assertEquals(1, result.assignments().get(1).orders().size());
    }

    @Test
    void highPriorityWinsWhenCapacityIsTight() {
        var result = strategy.plan(
                List.of(order("LOW", 10, Priority.LOW), order("HIGH", 10, Priority.HIGH)),
                List.of(vehicle("V1", 10)));

        assertEquals("HIGH", result.assignments().get(0).orders().get(0).getOrderId());
        assertEquals("LOW", result.unassignedOrders().get(0).order().getOrderId());
    }
}
