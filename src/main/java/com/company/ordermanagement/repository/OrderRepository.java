package com.company.ordermanagement.repository;

import com.company.ordermanagement.entity.Order;
import com.company.ordermanagement.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerNameContainingIgnoreCase(String customerName);
}
