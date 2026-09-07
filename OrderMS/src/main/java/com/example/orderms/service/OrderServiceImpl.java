package com.example.orderms.service;

import com.example.orderms.dto.CreateOrderRequest;
import com.example.orderms.dto.OrderResponse;
import com.example.orderms.entity.Order;
import com.example.orderms.entity.OrderStatus;
import com.example.orderms.repository.OrderRepository;
import org.springframework.stereotype.Service;
import com.example.orderms.exception.OrderNotFoundException;
import com.example.orderms.security.PaymentEncryptionService;
import com.example.orderms.producer.OrderEventProducer;
import com.example.orderms.event.OrderPlacedEvent;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentEncryptionService paymentEncryptionService;
    private final OrderEventProducer orderEventProducer;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            PaymentEncryptionService paymentEncryptionService,
            OrderEventProducer orderEventProducer) {

        this.orderRepository = orderRepository;
        this.paymentEncryptionService = paymentEncryptionService;
        this.orderEventProducer = orderEventProducer;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {

        Order order = new Order();

        order.setCustomerName(request.getCustomerName());
        order.setProduct(request.getProduct());
        order.setQuantity(request.getQuantity());
        order.setTotal(request.getTotal());

        // Estos valores los controla el backend.
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        String encryptedCardData =
                paymentEncryptionService.encrypt(request.getCard());

        OrderPlacedEvent event = new OrderPlacedEvent(
                savedOrder.getId(),
                savedOrder.getTotal(),
                encryptedCardData
        );

        orderEventProducer.sendOrderPlacedEvent(event);


        return toResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return toResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderResponse toResponse(Order order) {

        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getProduct(),
                order.getQuantity(),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }


}