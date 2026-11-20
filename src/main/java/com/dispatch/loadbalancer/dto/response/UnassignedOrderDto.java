package com.dispatch.loadbalancer.dto.response;

public record UnassignedOrderDto(OrderDto order, String reason) {
}
