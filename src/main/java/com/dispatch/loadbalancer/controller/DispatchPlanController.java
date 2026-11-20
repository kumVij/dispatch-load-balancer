package com.dispatch.loadbalancer.controller;

import com.dispatch.loadbalancer.dto.response.ApiResponse;
import com.dispatch.loadbalancer.dto.response.DispatchPlanResponse;
import com.dispatch.loadbalancer.dto.response.StrategyDto;
import com.dispatch.loadbalancer.service.DataResetService;
import com.dispatch.loadbalancer.service.DispatchPlanService;
import com.dispatch.loadbalancer.strategy.DispatchStrategyFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/dispatch", "/api/v1/dispatch"})
@Tag(name = "Dispatch Plan")
public class DispatchPlanController {

    private final DispatchPlanService planService;
    private final DispatchStrategyFactory strategyFactory;
    private final DataResetService resetService;

    public DispatchPlanController(DispatchPlanService planService, DispatchStrategyFactory strategyFactory,
                                  DataResetService resetService) {
        this.planService = planService;
        this.strategyFactory = strategyFactory;
        this.resetService = resetService;
    }

    @GetMapping("/plan")
    @Operation(summary = "Compute the dispatch plan from stored orders and vehicles (not persisted)")
    public DispatchPlanResponse getPlan(@RequestParam(required = false) String strategy) {
        return planService.generatePlan(strategy);
    }

    @GetMapping("/strategies")
    @Operation(summary = "List the available allocation strategies")
    public List<StrategyDto> listStrategies() {
        return strategyFactory.available().stream()
                .map(s -> new StrategyDto(s.name(), s.description(),
                        s.name().equals(DispatchStrategyFactory.DEFAULT)))
                .toList();
    }

    @DeleteMapping("/reset")
    @Operation(summary = "Delete all stored orders and vehicles (and optionally plan history)")
    public ApiResponse reset(@RequestParam(defaultValue = "false") boolean includePlans) {
        resetService.reset(includePlans);
        return ApiResponse.success(includePlans
                ? "All orders, vehicles and plans cleared."
                : "All orders and vehicles cleared.");
    }
}
