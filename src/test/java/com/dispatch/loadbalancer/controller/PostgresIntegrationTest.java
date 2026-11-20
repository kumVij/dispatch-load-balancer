package com.dispatch.loadbalancer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.dispatch.loadbalancer.controller.TestPayloads.order;
import static com.dispatch.loadbalancer.controller.TestPayloads.orders;
import static com.dispatch.loadbalancer.controller.TestPayloads.vehicle;
import static com.dispatch.loadbalancer.controller.TestPayloads.vehicles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the Flyway migrations and the persistence flow against a real PostgreSQL container.
 * Skipped automatically when Docker is not available.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class PostgresIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mvc;

    @Test
    void migrationsRunAndPlansPersistOnPostgres() throws Exception {
        mvc.perform(delete("/api/dispatch/reset").param("includePlans", "true")).andExpect(status().isOk());
        mvc.perform(post("/api/dispatch/vehicles").contentType(MediaType.APPLICATION_JSON)
                .content(vehicles(vehicle("V1", 100, 28.6139, 77.2090)))).andExpect(status().isOk());
        mvc.perform(post("/api/dispatch/orders").contentType(MediaType.APPLICATION_JSON)
                .content(orders(order("O1", 28.5355, 77.3910, 10, "HIGH"),
                        order("O2", 28.4595, 77.0266, 20, "MEDIUM")))).andExpect(status().isOk());

        mvc.perform(post("/api/dispatch/plans")).andExpect(status().isCreated());

        mvc.perform(get("/api/dispatch/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].assignedOrders").value(2));
    }
}
