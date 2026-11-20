package com.dispatch.loadbalancer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static com.dispatch.loadbalancer.controller.TestPayloads.order;
import static com.dispatch.loadbalancer.controller.TestPayloads.orders;
import static com.dispatch.loadbalancer.controller.TestPayloads.vehicle;
import static com.dispatch.loadbalancer.controller.TestPayloads.vehicles;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ObservabilityIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void clean() throws Exception {
        mvc.perform(delete("/api/dispatch/reset").param("includePlans", "true")).andExpect(status().isOk());
    }

    @Test
    void healthEndpointReportsUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void echoesAnIncomingCorrelationId() throws Exception {
        mvc.perform(get("/api/dispatch/orders").header("X-Correlation-Id", "test-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "test-123"));
    }

    @Test
    void generatesACorrelationIdWhenMissing() throws Exception {
        mvc.perform(get("/api/dispatch/orders")).andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void replacesAnUnsafeCorrelationId() throws Exception {
        MvcResult result = mvc.perform(get("/api/dispatch/orders").header("X-Correlation-Id", "bad id!"))
                .andReturn();
        assertNotEquals("bad id!", result.getResponse().getHeader("X-Correlation-Id"));
    }

    @Test
    void errorBodiesCarryTheCorrelationId() throws Exception {
        mvc.perform(get("/api/dispatch/orders/NOPE").header("X-Correlation-Id", "abc-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.correlationId").value("abc-1"));
    }

    @Test
    void unknownPathsReturnJson404NotA500() throws Exception {
        mvc.perform(get("/api/dispatch/nothing-here"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void wrongMethodReturnsJson405() throws Exception {
        mvc.perform(patch("/api/dispatch/plan"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void planGenerationRecordsATimerMetric() throws Exception {
        mvc.perform(post("/api/dispatch/vehicles").contentType(MediaType.APPLICATION_JSON)
                .content(vehicles(vehicle("V1", 20, 0, 0)))).andExpect(status().isOk());
        mvc.perform(post("/api/dispatch/orders").contentType(MediaType.APPLICATION_JSON)
                .content(orders(order("O1", 0.1, 0.1, 10, "HIGH")))).andExpect(status().isOk());
        mvc.perform(get("/api/dispatch/plan")).andExpect(status().isOk());

        mvc.perform(get("/actuator/metrics/dispatch.plan.duration")).andExpect(status().isOk());
    }
}
