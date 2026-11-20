package com.dispatch.loadbalancer.strategy;

import com.dispatch.loadbalancer.model.DeliveryOrder;

public record UnassignedOrder(DeliveryOrder order, String reason) {
}
