package com.dispatch.loadbalancer.dto.response;

import java.util.List;

public record DispatchPlanResponse(List<AssignedVehicleDto> dispatchPlan,
                                   List<UnassignedOrderDto> unassignedOrders) {
}
