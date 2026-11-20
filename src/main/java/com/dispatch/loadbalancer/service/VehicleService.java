package com.dispatch.loadbalancer.service;

import com.dispatch.loadbalancer.dto.request.VehiclePatchRequest;
import com.dispatch.loadbalancer.dto.request.VehicleRequest;
import com.dispatch.loadbalancer.exception.InvalidRequestException;
import com.dispatch.loadbalancer.exception.ResourceNotFoundException;
import com.dispatch.loadbalancer.mapper.DispatchMapper;
import com.dispatch.loadbalancer.model.Vehicle;
import com.dispatch.loadbalancer.repository.VehicleRepository;
import com.dispatch.loadbalancer.repository.VehicleSpecifications;
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
public class VehicleService {

    static final Set<String> SORTABLE_FIELDS =
            Set.of("vehicleId", "capacity", "currentLatitude", "currentLongitude", "currentAddress");

    private final VehicleRepository repository;
    private final DispatchMapper mapper;

    public VehicleService(VehicleRepository repository, DispatchMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Upsert: an existing vehicleId is updated, a new one is inserted. */
    @Transactional
    public int saveVehicles(List<VehicleRequest> requests) {
        RequestValidator.requireUniqueIds(requests.stream().map(r -> r.vehicleId().trim()).toList(), "vehicleId(s)");
        repository.saveAll(requests.stream().map(mapper::toEntity).toList());
        return requests.size();
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findAll() {
        return repository.findAll(Sort.by("vehicleId"));
    }

    @Transactional(readOnly = true)
    public Page<Vehicle> search(Double minCapacity, Double maxCapacity, String addressContains, Pageable pageable) {
        if (minCapacity != null && maxCapacity != null && minCapacity > maxCapacity) {
            throw new InvalidRequestException("minCapacity must be less than or equal to maxCapacity");
        }
        SortValidator.requireAllowed(pageable, SORTABLE_FIELDS);
        return repository.findAll(VehicleSpecifications.filter(minCapacity, maxCapacity, addressContains), pageable);
    }

    @Transactional(readOnly = true)
    public Vehicle findById(String vehicleId) {
        return repository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));
    }

    /** Full replace of an existing vehicle. The id in the path and body must match. */
    @Transactional
    public Vehicle replace(String vehicleId, VehicleRequest request) {
        String bodyId = request.vehicleId().trim();
        if (!vehicleId.equals(bodyId)) {
            throw new InvalidRequestException(
                    "vehicleId in path ('" + vehicleId + "') does not match body ('" + bodyId + "')");
        }
        findById(vehicleId);
        return repository.save(mapper.toEntity(request));
    }

    /** Partial update: only the supplied fields change. */
    @Transactional
    public Vehicle patch(String vehicleId, VehiclePatchRequest patch) {
        if (patch.hasNoChanges()) {
            throw new InvalidRequestException("PATCH body must contain at least one field to change");
        }
        Vehicle current = findById(vehicleId);
        Vehicle updated = new Vehicle(
                current.getVehicleId(),
                patch.capacity() != null ? patch.capacity() : current.getCapacity(),
                patch.currentLatitude() != null ? patch.currentLatitude() : current.getCurrentLatitude(),
                patch.currentLongitude() != null ? patch.currentLongitude() : current.getCurrentLongitude(),
                patch.currentAddress() != null ? patch.currentAddress().trim() : current.getCurrentAddress());
        return repository.save(updated);
    }

    @Transactional
    public void delete(String vehicleId) {
        if (!repository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found: " + vehicleId);
        }
        repository.deleteById(vehicleId);
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAllInBatch();
    }
}
