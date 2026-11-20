package com.dispatch.loadbalancer.service;

import com.dispatch.loadbalancer.dto.response.DispatchPlanResponse;
import com.dispatch.loadbalancer.exception.InsufficientDataException;
import com.dispatch.loadbalancer.mapper.DispatchMapper;
import com.dispatch.loadbalancer.model.DeliveryOrder;
import com.dispatch.loadbalancer.model.Vehicle;
import com.dispatch.loadbalancer.strategy.DispatchResult;
import com.dispatch.loadbalancer.strategy.DispatchStrategy;
import com.dispatch.loadbalancer.strategy.DispatchStrategyFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DispatchPlanService {

    private static final Logger log = LoggerFactory.getLogger(DispatchPlanService.class);

    private final OrderService orderService;
    private final VehicleService vehicleService;
    private final DispatchStrategyFactory strategyFactory;
    private final DispatchMapper mapper;
    private final MeterRegistry meterRegistry;

    public DispatchPlanService(OrderService orderService, VehicleService vehicleService,
                               DispatchStrategyFactory strategyFactory, DispatchMapper mapper,
                               MeterRegistry meterRegistry) {
        this.orderService = orderService;
        this.vehicleService = vehicleService;
        this.strategyFactory = strategyFactory;
        this.mapper = mapper;
        this.meterRegistry = meterRegistry;
    }

    /** Computes a plan from the stored orders and vehicles without persisting it. */
    @Transactional(readOnly = true)
    public DispatchPlanResponse generatePlan(String strategyName) {
        return compute(strategyName).plan();
    }

    @Transactional(readOnly = true)
    public ComputedPlan compute(String strategyName) {
        DispatchStrategy strategy = strategyFactory.resolve(strategyName);
        List<DeliveryOrder> orders = orderService.findAll();
        List<Vehicle> vehicles = vehicleService.findAll();
        if (orders.isEmpty()) {
            throw new InsufficientDataException("No delivery orders submitted. POST /api/v1/dispatch/orders first.");
        }
        if (vehicles.isEmpty()) {
            throw new InsufficientDataException("No vehicles submitted. POST /api/v1/dispatch/vehicles first.");
        }

        long startNanos = System.nanoTime();
        Timer.Sample sample = Timer.start(meterRegistry);
        DispatchResult result;
        try {
            result = strategy.plan(orders, vehicles);
        } finally {
            sample.stop(Timer.builder("dispatch.plan.duration")
                    .description("Time spent computing a dispatch plan")
                    .tag("strategy", strategy.name())
                    .register(meterRegistry));
        }
        long millis = (System.nanoTime() - startNanos) / 1_000_000;

        meterRegistry.counter("dispatch.plans.generated", "strategy", strategy.name()).increment();
        meterRegistry.counter("dispatch.orders.unassigned", "strategy", strategy.name())
                .increment(result.unassignedOrders().size());
        log.info("plan computed strategy={} orders={} vehicles={} unassigned={} durationMs={}",
                strategy.name(), orders.size(), vehicles.size(), result.unassignedOrders().size(), millis);

        return new ComputedPlan(strategy.name(), mapper.toResponse(result), millis);
    }
}
