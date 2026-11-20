package com.dispatch.loadbalancer.dto.response;

public record PlanDetailResponse(PlanMetadataDto metadata, DispatchPlanResponse plan) {
}
