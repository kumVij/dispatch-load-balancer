package com.dispatch.loadbalancer.controller;

import com.dispatch.loadbalancer.dto.response.PageResponse;
import com.dispatch.loadbalancer.dto.response.PlanDetailResponse;
import com.dispatch.loadbalancer.dto.response.PlanMetadataDto;
import com.dispatch.loadbalancer.dto.response.PlanSummaryResponse;
import com.dispatch.loadbalancer.service.PlanHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping({"/api/dispatch/plans", "/api/v1/dispatch/plans"})
@Tag(name = "Plan History")
public class PlanHistoryController {

    private final PlanHistoryService historyService;

    public PlanHistoryController(PlanHistoryService historyService) {
        this.historyService = historyService;
    }

    @PostMapping
    @Operation(summary = "Generate a plan from the stored orders/vehicles and save it")
    public ResponseEntity<PlanDetailResponse> createPlan(@RequestParam(required = false) String strategy) {
        PlanDetailResponse created = historyService.create(strategy);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.metadata().planId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "List saved plans (newest first by default)")
    public PageResponse<PlanMetadataDto> listPlans(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return PageResponse.from(historyService.list(pageable));
    }

    @GetMapping("/{planId}")
    @Operation(summary = "Get one saved plan with its full assignments")
    public PlanDetailResponse getPlan(@PathVariable String planId) {
        return historyService.get(planId);
    }

    @GetMapping("/{planId}/summary")
    @Operation(summary = "KPIs for a saved plan: utilization, assignment rate, distance, idle vehicles")
    public PlanSummaryResponse getSummary(@PathVariable String planId) {
        return historyService.summary(planId);
    }

    @DeleteMapping("/{planId}")
    @Operation(summary = "Delete a saved plan")
    public ResponseEntity<Void> deletePlan(@PathVariable String planId) {
        historyService.delete(planId);
        return ResponseEntity.noContent().build();
    }
}
