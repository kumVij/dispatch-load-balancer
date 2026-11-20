package com.dispatch.loadbalancer.dto.request;

import com.dispatch.loadbalancer.model.Priority;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Partial update: any field left out (null) keeps its stored value. */
public record OrderPatchRequest(
        @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "latitude must be <= 90") Double latitude,
        @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "longitude must be <= 180") Double longitude,
        @Size(max = 255, message = "address must be at most 255 characters")
        @Pattern(regexp = ".*\\S.*", message = "address must not be blank") String address,
        @Positive(message = "packageWeight must be greater than 0") Double packageWeight,
        Priority priority) {

    public boolean hasNoChanges() {
        return latitude == null && longitude == null && address == null
                && packageWeight == null && priority == null;
    }
}
