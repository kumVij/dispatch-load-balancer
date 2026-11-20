package com.dispatch.loadbalancer.dto.request;

import com.dispatch.loadbalancer.model.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OrderRequest(
        @Schema(example = "ORD001", description = "Unique order id")
        @NotBlank(message = "orderId is required")
        @Size(max = 64, message = "orderId must be at most 64 characters")
        @Pattern(regexp = "^[A-Za-z0-9._-]*$",
                message = "orderId may contain only letters, digits, '.', '_' and '-'") String orderId,
        @Schema(example = "12.9716", description = "Drop-point latitude (-90 to 90)")
        @NotNull(message = "latitude is required")
        @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "latitude must be <= 90") Double latitude,
        @Schema(example = "77.5946", description = "Drop-point longitude (-180 to 180)")
        @NotNull(message = "longitude is required")
        @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "longitude must be <= 180") Double longitude,
        @Schema(example = "MG Road, Bangalore, Karnataka, India")
        @NotBlank(message = "address is required")
        @Size(max = 255, message = "address must be at most 255 characters") String address,
        @Schema(example = "10", description = "Package weight, same unit as vehicle capacity")
        @NotNull(message = "packageWeight is required")
        @Positive(message = "packageWeight must be greater than 0") Double packageWeight,
        @Schema(example = "HIGH")
        @NotNull(message = "priority is required (HIGH, MEDIUM or LOW)") Priority priority) {
}
