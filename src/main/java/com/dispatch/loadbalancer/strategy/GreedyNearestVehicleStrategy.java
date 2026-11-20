package com.dispatch.loadbalancer.strategy;

import com.dispatch.loadbalancer.model.DeliveryOrder;
import com.dispatch.loadbalancer.model.Vehicle;
import com.dispatch.loadbalancer.util.HaversineCalculator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Priority-aware greedy allocation (default strategy):
 * <ol>
 *   <li>Sort orders HIGH -> LOW, then heavier first, then by id (deterministic).</li>
 *   <li>Give each order to the closest vehicle (measured from that vehicle's last stop,
 *       or its start position) that still has enough remaining capacity.</li>
 *   <li>Orders no vehicle can carry go to {@code unassignedOrders} with a reason.</li>
 *   <li>Each vehicle's stop sequence is then shortened with 2-opt (open route, no return leg).</li>
 * </ol>
 * This is a heuristic - capacitated routing is NP-hard - so it is fast but not provably optimal.
 */
@Component
public class GreedyNearestVehicleStrategy implements DispatchStrategy {

    public static final String NAME = "greedy";

    /** 2-opt is O(n^3) per pass in this simple form, so skip it for very long routes. */
    private static final int MAX_TWO_OPT_STOPS = 200;

    private final HaversineCalculator haversine;

    public GreedyNearestVehicleStrategy(HaversineCalculator haversine) {
        this.haversine = haversine;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return "Priority first, nearest vehicle with spare capacity, then 2-opt route improvement";
    }

    @Override
    public DispatchResult plan(List<DeliveryOrder> orders, List<Vehicle> vehicles) {
        List<VehicleState> states = StrategySupport.statesSortedById(vehicles);
        double largestCapacity = StrategySupport.largestCapacity(vehicles);

        List<UnassignedOrder> unassigned = new ArrayList<>();
        orders.stream().sorted(StrategySupport.ORDER_SEQUENCE).forEach(order -> {
            VehicleState best = findBestVehicle(states, order);
            if (best == null) {
                unassigned.add(new UnassignedOrder(order,
                        StrategySupport.unassignedReason(order, largestCapacity)));
            } else {
                best.add(order);
            }
        });

        List<VehicleAssignment> assignments = states.stream().map(this::toAssignment).toList();
        return new DispatchResult(assignments, unassigned);
    }

    private VehicleState findBestVehicle(List<VehicleState> states, DeliveryOrder order) {
        VehicleState best = null;
        double bestDistance = Double.MAX_VALUE;
        for (VehicleState state : states) {
            if (state.remaining + StrategySupport.EPS < order.getPackageWeight()) {
                continue;
            }
            double d = haversine.distanceKm(state.lat, state.lon, order.getLatitude(), order.getLongitude());
            boolean closer = d < bestDistance - StrategySupport.EPS;
            boolean tieWithMoreRoom = best != null
                    && Math.abs(d - bestDistance) <= StrategySupport.EPS
                    && state.remaining > best.remaining;
            if (best == null || closer || tieWithMoreRoom) {
                best = state;
                bestDistance = d;
            }
        }
        return best;
    }

    private VehicleAssignment toAssignment(VehicleState state) {
        List<DeliveryOrder> route = state.orders.size() <= MAX_TWO_OPT_STOPS
                ? improveWithTwoOpt(state.vehicle, state.orders)
                : new ArrayList<>(state.orders);
        return new VehicleAssignment(state.vehicle, route, StrategySupport.totalWeight(route),
                StrategySupport.pathDistance(haversine, state.vehicle, route));
    }

    private List<DeliveryOrder> improveWithTwoOpt(Vehicle vehicle, List<DeliveryOrder> route) {
        List<DeliveryOrder> best = new ArrayList<>(route);
        double bestDistance = StrategySupport.pathDistance(haversine, vehicle, best);
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 0; i < best.size() - 1; i++) {
                for (int j = i + 1; j < best.size(); j++) {
                    List<DeliveryOrder> candidate = new ArrayList<>(best);
                    Collections.reverse(candidate.subList(i, j + 1));
                    double d = StrategySupport.pathDistance(haversine, vehicle, candidate);
                    if (d < bestDistance - StrategySupport.EPS) {
                        best = candidate;
                        bestDistance = d;
                        improved = true;
                    }
                }
            }
        }
        return best;
    }
}
