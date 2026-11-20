package com.dispatch.loadbalancer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A persisted dispatch plan. Summary figures live in their own columns so the history list can be
 * paged and sorted in SQL; the full plan is kept as a JSON snapshot so later edits to orders or
 * vehicles never rewrite history.
 */
@Entity
@Table(name = "dispatch_plans")
public class StoredDispatchPlan {

    @Id
    @Column(name = "plan_id", nullable = false, updatable = false, length = 36)
    private String planId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, length = 64)
    private String strategy;

    @Column(name = "total_orders", nullable = false)
    private int totalOrders;

    @Column(name = "assigned_orders", nullable = false)
    private int assignedOrders;

    @Column(name = "unassigned_orders", nullable = false)
    private int unassignedOrders;

    @Column(name = "total_distance_km", nullable = false)
    private double totalDistanceKm;

    @Column(name = "fleet_utilization_percent", nullable = false)
    private double fleetUtilizationPercent;

    @Column(name = "computation_millis", nullable = false)
    private long computationMillis;

    @Column(name = "plan_json", nullable = false)
    private String planJson;

    protected StoredDispatchPlan() {
        // required by JPA
    }

    public StoredDispatchPlan(String planId, Instant createdAt, String strategy, int totalOrders,
                              int assignedOrders, int unassignedOrders, double totalDistanceKm,
                              double fleetUtilizationPercent, long computationMillis, String planJson) {
        this.planId = planId;
        this.createdAt = createdAt;
        this.strategy = strategy;
        this.totalOrders = totalOrders;
        this.assignedOrders = assignedOrders;
        this.unassignedOrders = unassignedOrders;
        this.totalDistanceKm = totalDistanceKm;
        this.fleetUtilizationPercent = fleetUtilizationPercent;
        this.computationMillis = computationMillis;
        this.planJson = planJson;
    }

    public String getPlanId() { return planId; }
    public Instant getCreatedAt() { return createdAt; }
    public String getStrategy() { return strategy; }
    public int getTotalOrders() { return totalOrders; }
    public int getAssignedOrders() { return assignedOrders; }
    public int getUnassignedOrders() { return unassignedOrders; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public double getFleetUtilizationPercent() { return fleetUtilizationPercent; }
    public long getComputationMillis() { return computationMillis; }
    public String getPlanJson() { return planJson; }
}
