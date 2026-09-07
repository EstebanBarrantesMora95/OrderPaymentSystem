package com.example.orderms.service;

import com.example.orderms.dto.CreateOrderRequest;
import com.example.orderms.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getAllOrders();
}