package com.example.order.service;

import com.example.order.entity.OrderEntity;
import com.example.order.exception.OrderNotFoundException;
import com.example.order.kafka.OrderCreatedProducer;
import com.example.order.repository.OrderRepository;
import com.example.order.web.model.OrderRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.UUID;

import static com.example.order.enums.OrderStatus.PENDING;
import static com.example.order.enums.OrderStatus.REJECTED;

@Slf4j
@Service
public class OrderService {

	private static final String ORDER_REJECTED_TOPIC = "order.rejected";

	private final OrderRepository orderRepository;
	private final OrderCreatedProducer orderCreatedProducer;
	private final ObjectMapper objectMapper;

	public OrderService(OrderRepository orderRepository,
						OrderCreatedProducer orderCreatedProducer,
						ObjectMapper objectMapper) {
		this.orderRepository = orderRepository;
		this.orderCreatedProducer = orderCreatedProducer;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public OrderEntity createOrder(OrderRequest orderRequest) {
		OrderEntity orderEntity = OrderEntity.builder()
				.id(UUID.randomUUID().toString())
				.product(orderRequest.getProduct())
				.amount(orderRequest.getAmount())
				.username(orderRequest.getUsername())
				.status(PENDING)
				.build();
		OrderEntity persistedOrder = orderRepository.save(orderEntity);
		orderCreatedProducer.sendMessage(persistedOrder);
		return persistedOrder;
	}

	@KafkaListener(topics = ORDER_REJECTED_TOPIC)
	public void consumeOrderRejected(String message) {
		log.info(MessageFormat.format("Consumer message {0} from topic {1}", message, ORDER_REJECTED_TOPIC));
		try {
			OrderRejectedMessage orderRejectedMessage = objectMapper.readValue(message, OrderRejectedMessage.class);
			orderRepository.findById(orderRejectedMessage.getOrderId())
					.ifPresentOrElse(entity -> {
						entity.setStatus(REJECTED);
						entity.setReason(orderRejectedMessage.getReason());
						orderRepository.save(entity);
						log.info(MessageFormat.format("Reject order {0}", message, entity.getId()));
					}, () -> {
						throw new OrderNotFoundException(orderRejectedMessage.orderId);
					});
		} catch (JsonProcessingException e) {
			log.error("Cannot parse order rejected message: " + message, e);
		}
	}

	@Data
	@NoArgsConstructor
	private static class OrderRejectedMessage {

		private String orderId;
		private String reason;

	}
}
