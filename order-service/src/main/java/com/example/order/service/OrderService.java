package com.example.order.service;

import com.example.order.entity.OrderEntity;
import com.example.order.kafka.OrderCreatedProducer;
import com.example.order.repository.OrderRepository;
import com.example.order.web.model.OrderRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

import static com.example.order.enums.OrderStatus.CREATE;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderCreatedProducer orderCreatedProducer;

	public OrderService(OrderRepository orderRepository,
						OrderCreatedProducer orderCreatedProducer) {
		this.orderRepository = orderRepository;
		this.orderCreatedProducer = orderCreatedProducer;
	}


	@Transactional
	public OrderEntity createOrder(OrderRequest orderRequest) {
		OrderEntity orderEntity = OrderEntity.builder()
				.uuid(UUID.randomUUID().toString())
				.product(orderRequest.getProduct())
				.amount(orderRequest.getAmount())
				.username(orderRequest.getUsername())
				.status(CREATE)
				.build();
		OrderEntity persistedOrder = orderRepository.save(orderEntity);
		orderCreatedProducer.sendMessage(persistedOrder);
		return persistedOrder;
	}
}
