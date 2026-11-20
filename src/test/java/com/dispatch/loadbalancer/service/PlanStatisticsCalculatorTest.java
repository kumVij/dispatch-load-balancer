package com.dispatch.loadbalancer.service;

import com.dispatch.loadbalancer.dto.response.AssignedVehicleDto;
import com.dispatch.loadbalancer.dto.response.DispatchPlanResponse;
import com.dispatch.loadbalancer.dto.response.OrderDto;
import com.dispatch.loadbalancer.dto.response.PlanStatistics;
import com.dispatch.loadbalancer.dto.response.UnassignedOrderDto;
import com.dispatch.loadbalancer.model.Priority;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlanStatisticsCalculatorTest {

    private final PlanStatisticsCalculator calculator = new PlanStatisticsCalculator();

    private static OrderDto order(String id, double weight, Priority p) {
        return new OrderDto(id, 1, 1, "addr", weight, p);
    }

    @Test
    void computesKpisForAMixedPlan() {
        var busy = new AssignedVehicleDto("V1", 100, 50, "10.00 km", 10.0,
                List.of(order("O1", 20, Priority.HIGH), order("O2", 30, Priority.MEDIUM)));
        var idle = new AssignedVehicleDto("V2", 100, 0, "0.00 km", 0.0, List.of());
        var plan = new DispatchPlanResponse(List.of(busy, idle),
                List.of(new UnassignedOrderDto(order("O3", 500, Priority.LOW), "too heavy")));

        PlanStatistics stats = calculator.calculate(plan);

        assertEquals(3, stats.totalOrders());
        assertEquals(2, stats.assignedOrders());
        assertEquals(1, stats.unassignedOrders());
        assertEquals(66.67, stats.assignmentRatePercent(), 1e-9);
        assertEquals(200.0, stats.fleetCapacity(), 1e-9);
        assertEquals(25.0, stats.fleetUtilizationPercent(), 1e-9);
        assertEquals(10.0, stats.totalDistanceKm(), 1e-9);
        assertEquals(1, stats.vehiclesUsed());
        assertEquals(1, stats.vehiclesIdle());
        assertEquals(1L, stats.unassignedByPriority().get(Priority.LOW));
        assertEquals(0L, stats.unassignedByPriority().get(Priority.HIGH));
        assertEquals(50.0, stats.vehicles().get(0).utilizationPercent(), 1e-9);
    }

    @Test
    void emptyPlanDoesNotDivideByZero() {
        PlanStatistics stats = calculator.calculate(new DispatchPlanResponse(List.of(), List.of()));

        assertEquals(0, stats.totalOrders());
        assertEquals(0.0, stats.assignmentRatePercent(), 1e-9);
        assertEquals(0.0, stats.fleetUtilizationPercent(), 1e-9);
    }
}
