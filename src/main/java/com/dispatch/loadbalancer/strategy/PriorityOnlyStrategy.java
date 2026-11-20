package com.dispatch.loadbalancer.strategy;

import com.dispatch.loadbalancer.model.DeliveryOrder;
import com.dispatch.loadbalancer.model.Vehicle;
import com.dispatch.loadbalancer.util.HaversineCalculator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Baseline strategy that respects priority and capacity but ignores distance: each order, taken
 * HIGH -> LOW, goes to the vehicle with the most spare capacity (spreading load evenly). Routes are
 * left in assignment order. Useful to compare against {@link GreedyNearestVehicleStrategy}.
 */
@Component
public class PriorityOnlyStrategy implements DispatchStrategy {

    public static final String NAME = "priority-only";

    private final HaversineCalculator haversine;

    public PriorityOnlyStrategy(HaversineCalculator haversine) {
        this.haversine = haversine;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return "Priority first, most spare capacity wins, distance ignored (baseline for comparison)";
    }

    @Override
    public DispatchResult plan(List<DeliveryOrder> orders, List<Vehicle> vehicles) {
        List<VehicleState> states = StrategySupport.statesSortedById(vehicles);
        double largestCapacity = StrategySupport.largestCapacity(vehicles);

        List<UnassignedOrder> unassigned = new ArrayList<>();
        orders.stream().sorted(StrategySupport.ORDER_SEQUENCE).forEach(order -> {
            VehicleState best = null;
            for (VehicleState state : states) {
                if (state.remaining + StrategySupport.EPS < order.getPackageWeight()) {
                    continue;
                }
                if (best == null || state.remaining > best.remaining + StrategySupport.EPS) {
                    best = state;
                }
            }
            if (best == null) {
                unassigned.add(new UnassignedOrder(order,
                        StrategySupport.unassignedReason(order, largestCapacity)));
            } else {
                best.add(order);
            }
        });

        List<VehicleAssignment> assignments = states.stream()
                .map(s -> new VehicleAssignment(s.vehicle, List.copyOf(s.orders),
                        StrategySupport.totalWeight(s.orders),
                        StrategySupport.pathDistance(haversine, s.vehicle, s.orders)))
                .toList();
        return new DispatchResult(assignments, unassigned);
    }
}
