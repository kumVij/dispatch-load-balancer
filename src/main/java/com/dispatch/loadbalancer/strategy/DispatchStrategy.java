package com.dispatch.loadbalancer.strategy;

import com.dispatch.loadbalancer.model.DeliveryOrder;
import com.dispatch.loadbalancer.model.Vehicle;

import java.util.List;

/**
 * Strategy pattern: each allocation algorithm is a Spring bean with a unique {@link #name()},
 * selectable per request through {@link DispatchStrategyFactory}.
 */
public interface DispatchStrategy {

    String name();

    String description();

    DispatchResult plan(List<DeliveryOrder> orders, List<Vehicle> vehicles);
}
