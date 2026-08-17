package com.company.ordermanagement.service;

import com.company.ordermanagement.dto.CreateOrderRequest;
import com.company.ordermanagement.dto.OrderResponse;
import com.company.ordermanagement.dto.PageResponse;
import com.company.ordermanagement.entity.Order;
import com.company.ordermanagement.entity.OrderStatus;
import com.company.ordermanagement.exception.InvalidStatusTransitionException;
import com.company.ordermanagement.exception.OrderNotFoundException;
import com.company.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = new EnumMap<>(Map.of(
            OrderStatus.CREATED, Set.of(OrderStatus.PAID, OrderStatus.CANCELED),
            OrderStatus.PAID, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELED),
            OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELED, Set.of()
    ));

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setTotalAmount(request.totalAmount());
        Order saved = orderRepository.save(order);
        log.info("Order created: {}", saved.getId());
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID id) {
        return OrderResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listOrders(Pageable pageable) {
        return PageResponse.from(orderRepository.findAll(pageable), OrderResponse::from);
    }

    @Transactional
    public OrderResponse updateStatus(UUID id, OrderStatus newStatus) {
        Order order = findOrThrow(id);
        OrderStatus currentStatus = order.getStatus();

        validateTransition(currentStatus, newStatus);
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        log.info("Order {} status updated: {} -> {}", id, currentStatus, newStatus);
        return OrderResponse.from(updated);
    }

    @Transactional
    public void cancelOrder(UUID id) {
        Order order = findOrThrow(id);
        validateTransition(order.getStatus(), OrderStatus.CANCELED);
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
        log.info("Order {} canceled", id);
    }

    private Order findOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id.toString()));
    }

    private void validateTransition(OrderStatus current, OrderStatus target) {
        Set<OrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new InvalidStatusTransitionException(current, target);
        }
    }
}
