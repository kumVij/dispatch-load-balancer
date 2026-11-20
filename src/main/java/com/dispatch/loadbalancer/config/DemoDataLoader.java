package com.dispatch.loadbalancer.config;

import com.dispatch.loadbalancer.dto.request.OrderBatchRequest;
import com.dispatch.loadbalancer.dto.request.VehicleBatchRequest;
import com.dispatch.loadbalancer.service.OrderService;
import com.dispatch.loadbalancer.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads the assignment's sample orders and vehicles at start-up when {@code app.seed-demo-data=true}
 * (enabled by the {@code prod} profile) so a freshly started public deployment is immediately demoable.
 * Uses the normal service layer, so it is an idempotent upsert.
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DemoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final OrderService orderService;
    private final VehicleService vehicleService;
    private final ObjectMapper objectMapper;

    public DemoDataLoader(OrderService orderService, VehicleService vehicleService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.vehicleService = vehicleService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        OrderBatchRequest orders = read("demo/sample-orders.json", OrderBatchRequest.class);
        VehicleBatchRequest vehicles = read("demo/sample-vehicles.json", VehicleBatchRequest.class);
        int savedOrders = orderService.saveOrders(orders.orders());
        int savedVehicles = vehicleService.saveVehicles(vehicles.vehicles());
        log.info("demo data loaded orders={} vehicles={}", savedOrders, savedVehicles);
    }

    private <T> T read(String path, Class<T> type) throws IOException {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readValue(in, type);
        }
    }
}
