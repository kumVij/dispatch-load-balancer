package com.dispatch.loadbalancer.controller;

import com.jayway.jsonpath.JsonPath;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlanApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void clean() throws Exception {
        mvc.perform(delete("/api/dispatch/reset").param("includePlans", "true")).andExpect(status().isOk());
    }

    private void seed() throws Exception {
        mvc.perform(post("/api/dispatch/vehicles").contentType(MediaType.APPLICATION_JSON)
                .content(vehicles(vehicle("V1", 20, 0, 0)))).andExpect(status().isOk());
        mvc.perform(post("/api/dispatch/orders").contentType(MediaType.APPLICATION_JSON)
                .content(orders(order("O1", 0.1, 0.1, 10, "HIGH"), order("O2", 0.2, 0.2, 15, "LOW"))))
                .andExpect(status().isOk());
    }

    private String createPlan(String strategyParam) throws Exception {
        var request = post("/api/v1/dispatch/plans");
        if (strategyParam != null) {
            request.param("strategy", strategyParam);
        }
        MvcResult result = mvc.perform(request).andExpect(status().isCreated())
                .andExpect(header().exists("Location")).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.metadata.planId");
    }

    @Test
    void createPlanPersistsItAndReturns201() throws Exception {
        seed();
        mvc.perform(post("/api/dispatch/plans"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.metadata.strategy").value("greedy"))
                .andExpect(jsonPath("$.metadata.assignedOrders").value(1))
                .andExpect(jsonPath("$.metadata.unassignedOrders").value(1))
                .andExpect(jsonPath("$.plan.dispatchPlan[0].vehicleId").value("V1"));
    }

    @Test
    void savedPlanCanBeListedFetchedSummarisedAndDeleted() throws Exception {
        seed();
        String id = createPlan(null);

        mvc.perform(get("/api/v1/dispatch/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].planId").value(id));

        mvc.perform(get("/api/v1/dispatch/plans/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan.unassignedOrders[0].order.orderId").value("O2"));

        mvc.perform(get("/api/v1/dispatch/plans/{id}/summary", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statistics.totalOrders").value(2))
                .andExpect(jsonPath("$.statistics.fleetUtilizationPercent").value(50.0))
                .andExpect(jsonPath("$.statistics.vehiclesUsed").value(1))
                .andExpect(jsonPath("$.statistics.unassignedByPriority.LOW").value(1));

        mvc.perform(delete("/api/v1/dispatch/plans/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/dispatch/plans/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void savedPlanIsASnapshotUnaffectedByLaterOrderChanges() throws Exception {
        seed();
        String id = createPlan(null);
        mvc.perform(delete("/api/dispatch/orders/O1")).andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/dispatch/plans/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan.dispatchPlan[0].assignedOrders[0].orderId").value("O1"));
    }

    @Test
    void strategyCanBeSelectedPerRequest() throws Exception {
        seed();
        mvc.perform(post("/api/dispatch/plans").param("strategy", "priority-only"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.metadata.strategy").value("priority-only"));
        mvc.perform(get("/api/dispatch/plan").param("strategy", "PRIORITY-ONLY"))
                .andExpect(status().isOk());
    }

    @Test
    void unknownStrategyReturns400() throws Exception {
        seed();
        mvc.perform(get("/api/dispatch/plan").param("strategy", "nope"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Available")));
    }

    @Test
    void strategiesEndpointListsBothAlgorithms() throws Exception {
        mvc.perform(get("/api/v1/dispatch/strategies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.name=='greedy')].defaultStrategy").value(true))
                .andExpect(jsonPath("$[?(@.name=='priority-only')]").exists());
    }

    @Test
    void versionedAndLegacyPathsServeTheSamePlan() throws Exception {
        seed();
        mvc.perform(get("/api/v1/dispatch/plan")).andExpect(status().isOk());
        mvc.perform(get("/api/dispatch/plan")).andExpect(status().isOk());
    }

    @Test
    void creatingAPlanWithoutDataReturns422() throws Exception {
        mvc.perform(post("/api/dispatch/plans")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void planListRejectsUnknownSortField() throws Exception {
        mvc.perform(get("/api/dispatch/plans").param("sort", "planJson"))
                .andExpect(status().isBadRequest());
    }
}
