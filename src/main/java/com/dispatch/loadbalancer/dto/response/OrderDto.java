package com.dispatch.loadbalancer.dto.response;

import com.dispatch.loadbalancer.model.Priority;

public record OrderDto(String orderId, double latitude, double longitude,
                       String address, double packageWeight, Priority priority) {
}
