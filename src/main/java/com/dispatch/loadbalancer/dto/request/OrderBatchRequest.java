package com.dispatch.loadbalancer.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderBatchRequest(
        @NotEmpty(message = "orders must contain at least one order")
        List<@NotNull(message = "orders must not contain null entries") @Valid OrderRequest> orders) {
}
