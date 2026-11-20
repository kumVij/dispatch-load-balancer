package com.dispatch.loadbalancer.service;

import com.dispatch.loadbalancer.dto.response.DispatchPlanResponse;
import com.dispatch.loadbalancer.dto.response.PlanDetailResponse;
import com.dispatch.loadbalancer.dto.response.PlanMetadataDto;
import com.dispatch.loadbalancer.dto.response.PlanStatistics;
import com.dispatch.loadbalancer.dto.response.PlanSummaryResponse;
import com.dispatch.loadbalancer.exception.ResourceNotFoundException;
import com.dispatch.loadbalancer.model.StoredDispatchPlan;
import com.dispatch.loadbalancer.repository.StoredDispatchPlanRepository;
import com.dispatch.loadbalancer.util.SortValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Creates, lists, reads and deletes persisted dispatch plans. */
@Service
public class PlanHistoryService {

    static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "strategy", "totalOrders", "assignedOrders",
            "unassignedOrders", "totalDistanceKm", "fleetUtilizationPercent", "computationMillis");

    private final StoredDispatchPlanRepository repository;
    private final DispatchPlanService planService;
    private final PlanStatisticsCalculator statisticsCalculator;
    private final ObjectMapper objectMapper;

    public PlanHistoryService(StoredDispatchPlanRepository repository, DispatchPlanService planService,
                              PlanStatisticsCalculator statisticsCalculator, ObjectMapper objectMapper) {
        this.repository = repository;
        this.planService = planService;
        this.statisticsCalculator = statisticsCalculator;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PlanDetailResponse create(String strategyName) {
        ComputedPlan computed = planService.compute(strategyName);
        PlanStatistics stats = statisticsCalculator.calculate(computed.plan());

        StoredDispatchPlan entity = new StoredDispatchPlan(
                UUID.randomUUID().toString(), Instant.now(), computed.strategy(),
                stats.totalOrders(), stats.assignedOrders(), stats.unassignedOrders(),
                stats.totalDistanceKm(), stats.fleetUtilizationPercent(),
                computed.computationMillis(), toJson(computed.plan()));
        repository.save(entity);
        return new PlanDetailResponse(toMetadata(entity), computed.plan());
    }

    @Transactional(readOnly = true)
    public Page<PlanMetadataDto> list(Pageable pageable) {
        SortValidator.requireAllowed(pageable, SORTABLE_FIELDS);
        return repository.findAll(pageable).map(this::toMetadata);
    }

    @Transactional(readOnly = true)
    public PlanDetailResponse get(String planId) {
        StoredDispatchPlan entity = find(planId);
        return new PlanDetailResponse(toMetadata(entity), fromJson(entity.getPlanJson()));
    }

    @Transactional(readOnly = true)
    public PlanSummaryResponse summary(String planId) {
        StoredDispatchPlan entity = find(planId);
        PlanStatistics stats = statisticsCalculator.calculate(fromJson(entity.getPlanJson()));
        return new PlanSummaryResponse(entity.getPlanId(), entity.getCreatedAt(), entity.getStrategy(),
                entity.getComputationMillis(), stats);
    }

    @Transactional
    public void delete(String planId) {
        if (!repository.existsById(planId)) {
            throw new ResourceNotFoundException("Plan not found: " + planId);
        }
        repository.deleteById(planId);
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAllInBatch();
    }

    private StoredDispatchPlan find(String planId) {
        return repository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));
    }

    private PlanMetadataDto toMetadata(StoredDispatchPlan e) {
        return new PlanMetadataDto(e.getPlanId(), e.getCreatedAt(), e.getStrategy(), e.getTotalOrders(),
                e.getAssignedOrders(), e.getUnassignedOrders(), e.getTotalDistanceKm(),
                e.getFleetUtilizationPercent(), e.getComputationMillis());
    }

    private String toJson(DispatchPlanResponse plan) {
        try {
            return objectMapper.writeValueAsString(plan);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize dispatch plan", ex);
        }
    }

    private DispatchPlanResponse fromJson(String json) {
        try {
            return objectMapper.readValue(json, DispatchPlanResponse.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Stored dispatch plan is unreadable", ex);
        }
    }
}
