package com.example.order.service;

import com.example.order.entity.OrderEntity;
import com.example.order.exception.OrderNotFoundException;
import com.example.order.kafka.OrderCreatedProducer;
import com.example.order.model.InvoiceApprovedMessage;
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
import java.util.UUID;

import static com.example.order.enums.OrderStatus.APPROVED;
import static com.example.order.enums.OrderStatus.PENDING;
import static com.example.order.enums.OrderStatus.REJECTED;

@Slf4j
@Service
public class OrderService {

	private static final String INVENTORY_INVALIDATED_TOPIC = "inventory.invalidated";
	private static final String PAYMENT_REJECTED_TOPIC = "payment.rejected";
	private static final String INVOICE_APPROVED_TOPIC = "invoice.approved";

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
				.address(orderRequest.getAddress())
				.status(PENDING)
				.build();
		OrderEntity persistedOrder = orderRepository.save(orderEntity);
		orderCreatedProducer.sendMessage(persistedOrder);
		return persistedOrder;
	}

	@KafkaListener(topics = INVENTORY_INVALIDATED_TOPIC)
	public void consumeOrderRejected(String message) throws JsonProcessingException {
		log.info("Consume message {} from topic {}", message, INVENTORY_INVALIDATED_TOPIC);
		OrderRejectedMessage orderRejectedMessage = objectMapper.readValue(message, OrderRejectedMessage.class);
		orderRepository.findById(orderRejectedMessage.getOrderId())
				.ifPresentOrElse(entity -> {
					entity.setStatus(REJECTED);
					entity.setReason(orderRejectedMessage.getReason());
					orderRepository.save(entity);
					log.info("Reject order {0}", message, entity.getId());
				}, () -> {
					throw new OrderNotFoundException(orderRejectedMessage.orderId);
				});
	}

	@Data
	@NoArgsConstructor
	private static class OrderRejectedMessage {

		private String orderId;
		private String reason;

	}

	@KafkaListener(topics = PAYMENT_REJECTED_TOPIC)
	public void consumePaymentRejectedMessage(String message) throws JsonProcessingException {
		log.info("Consume message {} from topic {}", message, PAYMENT_REJECTED_TOPIC);
		PaymentRejectedMessage paymentRejectedMessage = objectMapper.readValue(message, PaymentRejectedMessage.class);
		orderRepository.findById(paymentRejectedMessage.getOrderId())
				.ifPresentOrElse(entity -> {
					entity.setStatus(REJECTED);
					entity.setReason(paymentRejectedMessage.getReason());
					orderRepository.save(entity);
					log.info("Reject order {0}", message, entity.getId());
				}, () -> {
					throw new OrderNotFoundException(paymentRejectedMessage.orderId);
				});
	}

	@Data
	@NoArgsConstructor
	public class PaymentRejectedMessage {

		private String orderId;
		private String reason;
		String product;
		Integer amount;

	}

	@KafkaListener(topics = INVOICE_APPROVED_TOPIC)
	public void consumeInvoiceApprovedTopic(String message) throws JsonProcessingException {
		log.info("Consume message {} from topic {}", message, INVOICE_APPROVED_TOPIC);
		InvoiceApprovedMessage approvedMessage = objectMapper.readValue(message, InvoiceApprovedMessage.class);
		String orderId = approvedMessage.getOrderId();
		orderRepository.findById(orderId).ifPresentOrElse(orderEntity -> {
			orderEntity.setStatus(APPROVED);
			orderRepository.save(orderEntity);
		}, () -> {
			// handle in case order cannot find
		});
	}
}
