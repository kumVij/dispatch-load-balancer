package com.dispatch.loadbalancer.service;

import com.dispatch.loadbalancer.dto.request.OrderPatchRequest;
import com.dispatch.loadbalancer.dto.request.OrderRequest;
import com.dispatch.loadbalancer.exception.InvalidRequestException;
import com.dispatch.loadbalancer.exception.ResourceNotFoundException;
import com.dispatch.loadbalancer.mapper.DispatchMapper;
import com.dispatch.loadbalancer.model.DeliveryOrder;
import com.dispatch.loadbalancer.model.Priority;
import com.dispatch.loadbalancer.repository.DeliveryOrderRepository;
import com.dispatch.loadbalancer.repository.OrderSpecifications;
import com.dispatch.loadbalancer.util.RequestValidator;
import com.dispatch.loadbalancer.util.SortValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    static final Set<String> SORTABLE_FIELDS = Set.of("orderId", "latitude", "longitude", "address", "packageWeight");

    private final DeliveryOrderRepository repository;
    private final DispatchMapper mapper;

    public OrderService(DeliveryOrderRepository repository, DispatchMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Upsert: an existing orderId is updated, a new one is inserted. */
    @Transactional
    public int saveOrders(List<OrderRequest> requests) {
        RequestValidator.requireUniqueIds(requests.stream().map(r -> r.orderId().trim()).toList(), "orderId(s)");
        repository.saveAll(requests.stream().map(mapper::toEntity).toList());
        return requests.size();
    }

    @Transactional(readOnly = true)
    public List<DeliveryOrder> findAll() {
        return repository.findAll(Sort.by("orderId"));
    }

    @Transactional(readOnly = true)
    public Page<DeliveryOrder> search(Priority priority, Double minWeight, Double maxWeight,
                                      String addressContains, Pageable pageable) {
        if (minWeight != null && maxWeight != null && minWeight > maxWeight) {
            throw new InvalidRequestException("minWeight must be less than or equal to maxWeight");
        }
        SortValidator.requireAllowed(pageable, SORTABLE_FIELDS);
        return repository.findAll(OrderSpecifications.filter(priority, minWeight, maxWeight, addressContains),
                pageable);
    }

    @Transactional(readOnly = true)
    public DeliveryOrder findById(String orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    /** Full replace of an existing order. The id in the path and body must match. */
    @Transactional
    public DeliveryOrder replace(String orderId, OrderRequest request) {
        String bodyId = request.orderId().trim();
        if (!orderId.equals(bodyId)) {
            throw new InvalidRequestException(
                    "orderId in path ('" + orderId + "') does not match body ('" + bodyId + "')");
        }
        findById(orderId);
        return repository.save(mapper.toEntity(request));
    }

    /** Partial update: only the supplied fields change. */
    @Transactional
    public DeliveryOrder patch(String orderId, OrderPatchRequest patch) {
        if (patch.hasNoChanges()) {
            throw new InvalidRequestException("PATCH body must contain at least one field to change");
        }
        DeliveryOrder current = findById(orderId);
        DeliveryOrder updated = new DeliveryOrder(
                current.getOrderId(),
                patch.latitude() != null ? patch.latitude() : current.getLatitude(),
                patch.longitude() != null ? patch.longitude() : current.getLongitude(),
                patch.address() != null ? patch.address().trim() : current.getAddress(),
                patch.packageWeight() != null ? patch.packageWeight() : current.getPackageWeight(),
                patch.priority() != null ? patch.priority() : current.getPriority());
        return repository.save(updated);
    }

    @Transactional
    public void delete(String orderId) {
        if (!repository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }
        repository.deleteById(orderId);
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAllInBatch();
    }
}
