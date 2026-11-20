package com.dispatch.loadbalancer.repository;

import com.dispatch.loadbalancer.model.DeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DeliveryOrderRepository
        extends JpaRepository<DeliveryOrder, String>, JpaSpecificationExecutor<DeliveryOrder> {
}
