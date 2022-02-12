package com.example.order.web.controller;

import com.example.order.entity.OrderEntity;
import com.example.order.service.OrderService;
import com.example.order.web.model.OrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

	private final OrderService orderService;

	OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity createOrder(@RequestBody OrderRequest orderRequest) {
		OrderEntity orderEntity = orderService.createOrder(orderRequest);
		return ResponseEntity.ok(orderEntity);
	}
}
