package com.company.ordermanagement.dto;

import com.company.ordermanagement.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "Status is required")
        OrderStatus status
) {}
