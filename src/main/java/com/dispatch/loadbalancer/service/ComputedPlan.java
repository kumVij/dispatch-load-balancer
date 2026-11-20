package com.dispatch.loadbalancer.service;

import com.dispatch.loadbalancer.dto.response.DispatchPlanResponse;

/** A freshly computed (not yet persisted) plan together with how it was produced. */
public record ComputedPlan(String strategy, DispatchPlanResponse plan, long computationMillis) {
}
