package com.example.store.controller;

import com.example.store.dto.OrderCreateRequestDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.OrderRepository;
import com.example.store.service.OrderService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@Validated
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderService orderService;

    @GetMapping
    public List<OrderDTO> getAllOrders() {
        return orderMapper.ordersToOrderDTOs(orderRepository.findAll());
    }

    @GetMapping("/{id}")
    public OrderDTO getOrderByID(@PathVariable @Positive(message = "Order ID must be a positive number") Long id) {
        return orderMapper.orderToOrderDTO(orderRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + id)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDTO createOrder(@RequestBody OrderCreateRequestDTO order) {
        return orderMapper.orderToOrderDTO(orderService.createOrder(order));
    }
}
