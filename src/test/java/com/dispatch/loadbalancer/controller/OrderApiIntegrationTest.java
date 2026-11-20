package com.dispatch.loadbalancer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.dispatch.loadbalancer.controller.TestPayloads.order;
import static com.dispatch.loadbalancer.controller.TestPayloads.orders;
import static com.dispatch.loadbalancer.controller.TestPayloads.vehicle;
import static com.dispatch.loadbalancer.controller.TestPayloads.vehicles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void clean() throws Exception {
        mvc.perform(delete("/api/dispatch/reset").param("includePlans", "true")).andExpect(status().isOk());
        mvc.perform(post("/api/dispatch/orders").contentType(MediaType.APPLICATION_JSON).content(orders(
                order("O1", 1, 1, 1, "HIGH"), order("O2", 1, 2, 2, "LOW"), order("O3", 1, 3, 3, "HIGH"),
                order("O4", 1, 4, 4, "MEDIUM"), order("O5", 1, 5, 5, "LOW")))).andExpect(status().isOk());
    }

    @Test
    void pagesAndSortsOrders() throws Exception {
        mvc.perform(get("/api/dispatch/orders").param("size", "2").param("sort", "packageWeight,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].orderId").value("O5"))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void filtersByPriority() throws Exception {
        mvc.perform(get("/api/dispatch/orders").param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void filtersByWeightRange() throws Exception {
        mvc.perform(get("/api/dispatch/orders").param("minWeight", "2").param("maxWeight", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void filtersByAddressText() throws Exception {
        mvc.perform(get("/api/dispatch/orders").param("q", "ADDR O3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].orderId").value("O3"));
    }

    @Test
    void rejectsUnknownSortField() throws Exception {
        mvc.perform(get("/api/dispatch/orders").param("sort", "bogus"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void rejectsUnknownPriorityFilter() throws Exception {
        mvc.perform(get("/api/dispatch/orders").param("priority", "URGENT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvertedWeightRange() throws Exception {
        mvc.perform(get("/api/dispatch/orders").param("minWeight", "9").param("maxWeight", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putReplacesAnOrder() throws Exception {
        mvc.perform(put("/api/dispatch/orders/O1").contentType(MediaType.APPLICATION_JSON)
                        .content(order("O1", 2, 2, 7, "LOW")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packageWeight").value(7.0))
                .andExpect(jsonPath("$.priority").value("LOW"));
    }

    @Test
    void putRejectsMismatchedIdAndUnknownOrder() throws Exception {
        mvc.perform(put("/api/dispatch/orders/O1").contentType(MediaType.APPLICATION_JSON)
                        .content(order("O2", 2, 2, 7, "LOW")))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/dispatch/orders/NOPE").contentType(MediaType.APPLICATION_JSON)
                        .content(order("NOPE", 2, 2, 7, "LOW")))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchChangesOnlySuppliedFields() throws Exception {
        mvc.perform(patch("/api/dispatch/orders/O1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"packageWeight\":42}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packageWeight").value(42.0))
                .andExpect(jsonPath("$.address").value("addr O1"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void patchRejectsEmptyBodyAndInvalidValues() throws Exception {
        mvc.perform(patch("/api/dispatch/orders/O1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/dispatch/orders/O1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"packageWeight\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteRemovesTheOrder() throws Exception {
        mvc.perform(delete("/api/dispatch/orders/O1")).andExpect(status().isNoContent());
        mvc.perform(get("/api/dispatch/orders/O1")).andExpect(status().isNotFound());
    }

    @Test
    void vehicleCrudAndPaging() throws Exception {
        mvc.perform(post("/api/dispatch/vehicles").contentType(MediaType.APPLICATION_JSON)
                        .content(vehicles(vehicle("V1", 50, 0, 0), vehicle("V2", 150, 0, 0))))
                .andExpect(status().isOk());
        mvc.perform(get("/api/dispatch/vehicles").param("minCapacity", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].vehicleId").value("V2"));
        mvc.perform(patch("/api/dispatch/vehicles/V1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"capacity\":75}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(75.0));
        mvc.perform(put("/api/dispatch/vehicles/V1").contentType(MediaType.APPLICATION_JSON)
                        .content(vehicle("V1", 60, 1, 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(60.0));
        mvc.perform(delete("/api/dispatch/vehicles/V2")).andExpect(status().isNoContent());
        mvc.perform(get("/api/dispatch/vehicles/V2")).andExpect(status().isNotFound());
    }
}
