package com.dispatch.loadbalancer.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Partial update: any field left out (null) keeps its stored value. */
public record VehiclePatchRequest(
        @Positive(message = "capacity must be greater than 0") Double capacity,
        @DecimalMin(value = "-90.0", message = "currentLatitude must be >= -90")
        @DecimalMax(value = "90.0", message = "currentLatitude must be <= 90") Double currentLatitude,
        @DecimalMin(value = "-180.0", message = "currentLongitude must be >= -180")
        @DecimalMax(value = "180.0", message = "currentLongitude must be <= 180") Double currentLongitude,
        @Size(max = 255, message = "currentAddress must be at most 255 characters")
        @Pattern(regexp = ".*\\S.*", message = "currentAddress must not be blank") String currentAddress) {

    public boolean hasNoChanges() {
        return capacity == null && currentLatitude == null && currentLongitude == null && currentAddress == null;
    }
}
