package com.dispatch.loadbalancer.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VehicleBatchRequest(
        @NotEmpty(message = "vehicles must contain at least one vehicle")
        List<@NotNull(message = "vehicles must not contain null entries") @Valid VehicleRequest> vehicles) {
}
