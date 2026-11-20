package com.dispatch.loadbalancer.controller;

import com.dispatch.loadbalancer.dto.request.VehicleBatchRequest;
import com.dispatch.loadbalancer.dto.request.VehiclePatchRequest;
import com.dispatch.loadbalancer.dto.request.VehicleRequest;
import com.dispatch.loadbalancer.dto.response.ApiResponse;
import com.dispatch.loadbalancer.dto.response.PageResponse;
import com.dispatch.loadbalancer.dto.response.VehicleDto;
import com.dispatch.loadbalancer.mapper.DispatchMapper;
import com.dispatch.loadbalancer.service.VehicleService;
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

@RestController
@RequestMapping({"/api/dispatch/vehicles", "/api/v1/dispatch/vehicles"})
@Tag(name = "Fleet")
public class VehicleController {

    private final VehicleService vehicleService;
    private final DispatchMapper mapper;

    public VehicleController(VehicleService vehicleService, DispatchMapper mapper) {
        this.vehicleService = vehicleService;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Submit fleet details (upsert by vehicleId)")
    public ResponseEntity<ApiResponse> submitVehicles(@Valid @RequestBody VehicleBatchRequest request) {
        vehicleService.saveVehicles(request.vehicles());
        return ResponseEntity.ok(ApiResponse.success("Vehicle details accepted."));
    }

    @GetMapping
    @Operation(summary = "Search stored vehicles (filter, sort, page)")
    public PageResponse<VehicleDto> listVehicles(
            @RequestParam(required = false) Double minCapacity,
            @RequestParam(required = false) Double maxCapacity,
            @RequestParam(required = false, name = "q") String addressContains,
            @ParameterObject @PageableDefault(size = 20, sort = "vehicleId") Pageable pageable) {
        return PageResponse.from(
                vehicleService.search(minCapacity, maxCapacity, addressContains, pageable)
                        .map(mapper::toDto));
    }

    @GetMapping("/{vehicleId}")
    @Operation(summary = "Get one vehicle")
    public VehicleDto getVehicle(@PathVariable String vehicleId) {
        return mapper.toDto(vehicleService.findById(vehicleId));
    }

    @PutMapping("/{vehicleId}")
    @Operation(summary = "Replace an existing vehicle")
    public VehicleDto replaceVehicle(@PathVariable String vehicleId, @Valid @RequestBody VehicleRequest request) {
        return mapper.toDto(vehicleService.replace(vehicleId, request));
    }

    @PatchMapping("/{vehicleId}")
    @Operation(summary = "Partially update a vehicle")
    public VehicleDto patchVehicle(@PathVariable String vehicleId, @Valid @RequestBody VehiclePatchRequest request) {
        return mapper.toDto(vehicleService.patch(vehicleId, request));
    }

    @DeleteMapping("/{vehicleId}")
    @Operation(summary = "Delete one vehicle")
    public ResponseEntity<Void> deleteVehicle(@PathVariable String vehicleId) {
        vehicleService.delete(vehicleId);
        return ResponseEntity.noContent().build();
    }
}
