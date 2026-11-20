package com.dispatch.loadbalancer.mapper;

import com.dispatch.loadbalancer.dto.request.OrderRequest;
import com.dispatch.loadbalancer.dto.request.VehicleRequest;
import com.dispatch.loadbalancer.dto.response.AssignedVehicleDto;
import com.dispatch.loadbalancer.dto.response.DispatchPlanResponse;
import com.dispatch.loadbalancer.dto.response.OrderDto;
import com.dispatch.loadbalancer.dto.response.UnassignedOrderDto;
import com.dispatch.loadbalancer.dto.response.VehicleDto;
import com.dispatch.loadbalancer.model.DeliveryOrder;
import com.dispatch.loadbalancer.model.Vehicle;
import com.dispatch.loadbalancer.strategy.DispatchResult;
import com.dispatch.loadbalancer.strategy.UnassignedOrder;
import com.dispatch.loadbalancer.strategy.VehicleAssignment;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DispatchMapper {

    public DeliveryOrder toEntity(OrderRequest r) {
        return new DeliveryOrder(r.orderId().trim(), r.latitude(), r.longitude(),
                r.address().trim(), r.packageWeight(), r.priority());
    }

    public Vehicle toEntity(VehicleRequest r) {
        return new Vehicle(r.vehicleId().trim(), r.capacity(), r.currentLatitude(),
                r.currentLongitude(), r.currentAddress().trim());
    }

    public OrderDto toDto(DeliveryOrder o) {
        return new OrderDto(o.getOrderId(), o.getLatitude(), o.getLongitude(),
                o.getAddress(), o.getPackageWeight(), o.getPriority());
    }

    public VehicleDto toDto(Vehicle v) {
        return new VehicleDto(v.getVehicleId(), v.getCapacity(), v.getCurrentLatitude(),
                v.getCurrentLongitude(), v.getCurrentAddress());
    }

    public DispatchPlanResponse toResponse(DispatchResult result) {
        return new DispatchPlanResponse(
                result.assignments().stream().map(this::toDto).toList(),
                result.unassignedOrders().stream().map(this::toDto).toList());
    }

    private AssignedVehicleDto toDto(VehicleAssignment a) {
        return new AssignedVehicleDto(
                a.vehicle().getVehicleId(),
                a.vehicle().getCapacity(),
                a.totalLoad(),
                String.format(Locale.ROOT, "%.2f km", a.totalDistanceKm()),
                Math.round(a.totalDistanceKm() * 100.0) / 100.0,
                a.orders().stream().map(this::toDto).toList());
    }

    private UnassignedOrderDto toDto(UnassignedOrder u) {
        return new UnassignedOrderDto(toDto(u.order()), u.reason());
    }
}
