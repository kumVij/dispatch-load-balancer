package com.dispatch.loadbalancer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dispatchOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Dispatch Load Balancer API")
                .version("1.0.0")
                .description("""
                        Assigns delivery orders to a fleet of vehicles respecting **capacity** and **priority** \
                        (HIGH, MEDIUM, LOW) while minimizing travel distance (Haversine).

                        **Typical flow:** 1) `POST /orders`  2) `POST /vehicles`  3) `GET /plan` \
                        (or `POST /plans` to save it, then `GET /plans/{id}/summary` for KPIs).

                        Orders that cannot be placed are returned in `unassignedOrders` with a reason. \
                        Every path is also served under the assignment's original `/api/dispatch` prefix. \
                        Responses carry an `X-Correlation-Id` header that also appears in error bodies."""));
    }
}
