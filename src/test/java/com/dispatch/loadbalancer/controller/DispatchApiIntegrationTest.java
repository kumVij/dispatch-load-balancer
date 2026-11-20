package com.dispatch.loadbalancer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DispatchApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void clean() throws Exception {
        mvc.perform(delete("/api/dispatch/reset").param("includePlans", "true")).andExpect(status().isOk());
    }

    private void postOrders(String json) throws Exception {
        mvc.perform(post("/api/dispatch/orders").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    private void postVehicles(String json) throws Exception {
        mvc.perform(post("/api/dispatch/vehicles").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void rejectsOrderWithInvalidLatitude() throws Exception {
        String body = """
                {"orders":[{"orderId":"O1","latitude":200,"longitude":77,
                "address":"x","packageWeight":5,"priority":"HIGH"}]}""";
        mvc.perform(post("/api/dispatch/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void rejectsUnknownPriority() throws Exception {
        String body = """
                {"orders":[{"orderId":"O1","latitude":10,"longitude":77,
                "address":"x","packageWeight":5,"priority":"URGENT"}]}""";
        mvc.perform(post("/api/dispatch/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsDuplicateOrderIdsInOneRequest() throws Exception {
        String body = """
                {"orders":[
                {"orderId":"O1","latitude":10,"longitude":77,"address":"x","packageWeight":5,"priority":"HIGH"},
                {"orderId":"O1","latitude":11,"longitude":77,"address":"y","packageWeight":5,"priority":"LOW"}]}""";
        mvc.perform(post("/api/dispatch/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void planWithoutDataReturns422() throws Exception {
        mvc.perform(get("/api/dispatch/plan")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void unknownOrderReturns404() throws Exception {
        mvc.perform(get("/api/dispatch/orders/NOPE")).andExpect(status().isNotFound());
    }

    @Test
    void fullFlowAssignsHighPriorityAndReportsUnassigned() throws Exception {
        postVehicles("""
                {"vehicles":[{"vehicleId":"V1","capacity":20,"currentLatitude":0,"currentLongitude":0,
                "currentAddress":"Depot"}]}""");
        postOrders("""
                {"orders":[
                {"orderId":"O1","latitude":0.1,"longitude":0.1,"address":"A","packageWeight":10,"priority":"HIGH"},
                {"orderId":"O2","latitude":0.2,"longitude":0.2,"address":"B","packageWeight":15,"priority":"LOW"}]}""");

        mvc.perform(get("/api/dispatch/plan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dispatchPlan[0].vehicleId").value("V1"))
                .andExpect(jsonPath("$.dispatchPlan[0].totalLoad").value(10.0))
                .andExpect(jsonPath("$.dispatchPlan[0].assignedOrders[0].orderId").value("O1"))
                .andExpect(jsonPath("$.unassignedOrders.length()").value(1))
                .andExpect(jsonPath("$.unassignedOrders[0].order.orderId").value("O2"));
    }

    @Test
    void resubmittingAnOrderUpdatesInsteadOfDuplicating() throws Exception {
        String first = """
                {"orders":[{"orderId":"O1","latitude":10,"longitude":77,"address":"x","packageWeight":5,"priority":"HIGH"}]}""";
        String second = """
                {"orders":[{"orderId":"O1","latitude":10,"longitude":77,"address":"x","packageWeight":9,"priority":"HIGH"}]}""";
        postOrders(first);
        postOrders(second);

        mvc.perform(get("/api/dispatch/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].packageWeight").value(9.0));
    }
}
