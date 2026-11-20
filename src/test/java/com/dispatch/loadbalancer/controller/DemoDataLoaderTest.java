package com.dispatch.loadbalancer.controller;

import com.dispatch.loadbalancer.service.OrderService;
import com.dispatch.loadbalancer.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = "app.seed-demo-data=true")
class DemoDataLoaderTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private VehicleService vehicleService;

    @Test
    void sampleDataIsLoadedAtStartup() {
        assertNotNull(orderService.findById("ORD001"));
        assertEquals(40.0, orderService.findById("ORD006").getPackageWeight());
        assertNotNull(orderService.findById("ORD030"));
        assertEquals(120.0, vehicleService.findById("VEH003").getCapacity());
        assertNotNull(vehicleService.findById("VEH005"));
    }
}
