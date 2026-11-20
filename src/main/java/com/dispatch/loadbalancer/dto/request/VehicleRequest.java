package com.dispatch.loadbalancer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
        @Schema(example = "VEH001", description = "Unique vehicle id")
        @NotBlank(message = "vehicleId is required")
        @Size(max = 64, message = "vehicleId must be at most 64 characters")
        @Pattern(regexp = "^[A-Za-z0-9._-]*$",
                message = "vehicleId may contain only letters, digits, '.', '_' and '-'") String vehicleId,
        @Schema(example = "100", description = "Maximum total load the vehicle can carry")
        @NotNull(message = "capacity is required")
        @Positive(message = "capacity must be greater than 0") Double capacity,
        @Schema(example = "12.9716", description = "Current latitude (-90 to 90)")
        @NotNull(message = "currentLatitude is required")
        @DecimalMin(value = "-90.0", message = "currentLatitude must be >= -90")
        @DecimalMax(value = "90.0", message = "currentLatitude must be <= 90") Double currentLatitude,
        @Schema(example = "77.6413", description = "Current longitude (-180 to 180)")
        @NotNull(message = "currentLongitude is required")
        @DecimalMin(value = "-180.0", message = "currentLongitude must be >= -180")
        @DecimalMax(value = "180.0", message = "currentLongitude must be <= 180") Double currentLongitude,
        @Schema(example = "Indiranagar, Bangalore, Karnataka, India")
        @NotBlank(message = "currentAddress is required")
        @Size(max = 255, message = "currentAddress must be at most 255 characters") String currentAddress) {
}
