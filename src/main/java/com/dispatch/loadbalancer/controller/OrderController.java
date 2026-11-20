package com.dispatch.loadbalancer.controller;

import com.dispatch.loadbalancer.dto.request.OrderBatchRequest;
import com.dispatch.loadbalancer.dto.request.OrderPatchRequest;
import com.dispatch.loadbalancer.dto.request.OrderRequest;
import com.dispatch.loadbalancer.dto.response.ApiResponse;
import com.dispatch.loadbalancer.dto.response.OrderDto;
import com.dispatch.loadbalancer.dto.response.PageResponse;
import com.dispatch.loadbalancer.mapper.DispatchMapper;
import com.dispatch.loadbalancer.model.Priority;
import com.dispatch.loadbalancer.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Reachable at both /api/dispatch/... (as in the assignment) and the versioned /api/v1/dispatch/.... */
@RestController
@RequestMapping({"/api/dispatch/orders", "/api/v1/dispatch/orders"})
@Tag(name = "Delivery Orders")
public class OrderController {

    private final OrderService orderService;
    private final DispatchMapper mapper;

    public OrderController(OrderService orderService, DispatchMapper mapper) {
        this.orderService = orderService;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Submit delivery orders (upsert by orderId)")
    public ResponseEntity<ApiResponse> submitOrders(@Valid @RequestBody OrderBatchRequest request) {
        orderService.saveOrders(request.orders());
        return ResponseEntity.ok(ApiResponse.success("Delivery orders accepted."));
    }

    @GetMapping
    @Operation(summary = "Search stored orders (filter, sort, page)")
    public PageResponse<OrderDto> listOrders(
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Double minWeight,
            @RequestParam(required = false) Double maxWeight,
            @RequestParam(required = false, name = "q") String addressContains,
            @ParameterObject @PageableDefault(size = 20, sort = "orderId") Pageable pageable) {
        return PageResponse.from(
                orderService.search(priority, minWeight, maxWeight, addressContains, pageable)
                        .map(mapper::toDto));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get one order")
    public OrderDto getOrder(@PathVariable String orderId) {
        return mapper.toDto(orderService.findById(orderId));
    }

    @PutMapping("/{orderId}")
    @Operation(summary = "Replace an existing order")
    public OrderDto replaceOrder(@PathVariable String orderId, @Valid @RequestBody OrderRequest request) {
        return mapper.toDto(orderService.replace(orderId, request));
    }

    @PatchMapping("/{orderId}")
    @Operation(summary = "Partially update an order")
    public OrderDto patchOrder(@PathVariable String orderId, @Valid @RequestBody OrderPatchRequest request) {
        return mapper.toDto(orderService.patch(orderId, request));
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Delete one order")
    public ResponseEntity<Void> deleteOrder(@PathVariable String orderId) {
        orderService.delete(orderId);
        return ResponseEntity.noContent().build();
    }
}
