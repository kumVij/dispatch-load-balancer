package com.dispatch.loadbalancer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataResetService {

    private final OrderService orderService;
    private final VehicleService vehicleService;
    private final PlanHistoryService planHistoryService;

    public DataResetService(OrderService orderService, VehicleService vehicleService,
                            PlanHistoryService planHistoryService) {
        this.orderService = orderService;
        this.vehicleService = vehicleService;
        this.planHistoryService = planHistoryService;
    }

    @Transactional
    public void reset(boolean includePlans) {
        orderService.deleteAll();
        vehicleService.deleteAll();
        if (includePlans) {
            planHistoryService.deleteAll();
        }
    }
}
