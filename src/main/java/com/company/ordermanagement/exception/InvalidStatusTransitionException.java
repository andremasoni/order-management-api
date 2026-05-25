package com.company.ordermanagement.exception;

import com.company.ordermanagement.entity.OrderStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(OrderStatus from, OrderStatus to) {
        super("Invalid status transition: " + from + " -> " + to);
    }
}
