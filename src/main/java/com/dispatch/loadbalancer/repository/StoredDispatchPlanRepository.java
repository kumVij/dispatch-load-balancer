package com.dispatch.loadbalancer.repository;

import com.dispatch.loadbalancer.model.StoredDispatchPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredDispatchPlanRepository extends JpaRepository<StoredDispatchPlan, String> {
}
