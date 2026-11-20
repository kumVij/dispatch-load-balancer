package com.dispatch.loadbalancer.dto.response;

import java.time.Instant;

public record PlanSummaryResponse(String planId, Instant createdAt, String strategy,
                                  long computationMillis, PlanStatistics statistics) {
}
