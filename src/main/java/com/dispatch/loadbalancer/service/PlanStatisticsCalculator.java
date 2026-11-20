package com.dispatch.loadbalancer.service;

import com.dispatch.loadbalancer.dto.response.AssignedVehicleDto;
import com.dispatch.loadbalancer.dto.response.DispatchPlanResponse;
import com.dispatch.loadbalancer.dto.response.PlanStatistics;
import com.dispatch.loadbalancer.dto.response.VehicleStatistics;
import com.dispatch.loadbalancer.model.Priority;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Derives KPIs (assignment rate, fleet utilization, distance, idle vehicles) from a plan. */
@Component
public class PlanStatisticsCalculator {

    public PlanStatistics calculate(DispatchPlanResponse plan) {
        List<AssignedVehicleDto> vehicles = plan.dispatchPlan();

        int assigned = vehicles.stream().mapToInt(v -> v.assignedOrders().size()).sum();
        int unassigned = plan.unassignedOrders().size();
        int total = assigned + unassigned;

        double fleetCapacity = vehicles.stream().mapToDouble(AssignedVehicleDto::capacity).sum();
        double assignedWeight = vehicles.stream().mapToDouble(AssignedVehicleDto::totalLoad).sum();
        double distance = vehicles.stream().mapToDouble(AssignedVehicleDto::totalDistanceKm).sum();
        int used = (int) vehicles.stream().filter(v -> !v.assignedOrders().isEmpty()).count();

        Map<Priority, Long> unassignedByPriority = new EnumMap<>(Priority.class);
        for (Priority p : Priority.values()) {
            unassignedByPriority.put(p, 0L);
        }
        plan.unassignedOrders().forEach(u -> unassignedByPriority.merge(u.order().priority(), 1L, Long::sum));

        List<VehicleStatistics> perVehicle = vehicles.stream()
                .map(v -> new VehicleStatistics(v.vehicleId(), v.capacity(), v.totalLoad(),
                        percent(v.totalLoad(), v.capacity()), v.assignedOrders().size(), v.totalDistanceKm()))
                .toList();

        return new PlanStatistics(total, assigned, unassigned, percent(assigned, total),
                round2(assignedWeight), round2(fleetCapacity), percent(assignedWeight, fleetCapacity),
                round2(distance), used, vehicles.size() - used, unassignedByPriority, perVehicle);
    }

    private static double percent(double part, double whole) {
        return whole <= 0 ? 0.0 : round2(part / whole * 100.0);
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
