package com.dispatch.loadbalancer.strategy;

import java.util.List;

public record DispatchResult(List<VehicleAssignment> assignments, List<UnassignedOrder> unassignedOrders) {
}
