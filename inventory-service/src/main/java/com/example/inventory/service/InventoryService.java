package com.example.inventory.service;

import com.example.inventory.entity.InventoryEntity;
import com.example.inventory.kafka.OrderRejectedProducer;
import com.example.inventory.kafka.PaymentRequestedProducer;
import com.example.inventory.model.OrderRejectedMessage;
import com.example.inventory.model.PaymentRequestedMessage;
import com.example.inventory.repository.InventoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Optional;

import static com.example.inventory.kafka.PaymentRequestedProducer.PAYMENT_REQUESTED_TOPIC;

@Slf4j
@Service
public class InventoryService {

	private static final String ORDER_CREATED_TOPIC = "order.created";
	private static final String ORDER_REJECTED_TOPIC = "order.rejected";

	private final ObjectMapper objectMapper;
	private final InventoryRepository inventoryRepository;
	private final OrderRejectedProducer orderRejectedProducer;
	private final PaymentRequestedProducer paymentRequestedProducer;

	public InventoryService(InventoryRepository inventoryRepository,
						    ObjectMapper objectMapper,
							OrderRejectedProducer orderRejectedProducer,
							PaymentRequestedProducer paymentRequestedProducer) {
		this.objectMapper = objectMapper;
		this.inventoryRepository = inventoryRepository;
		this.orderRejectedProducer = orderRejectedProducer;
		this.paymentRequestedProducer = paymentRequestedProducer;
	}

	@KafkaListener(topics = ORDER_CREATED_TOPIC)
	public void comsumeOrderCreated(String message) {
		log.info(MessageFormat.format("Consumer message {0} from topic {1}", message, ORDER_CREATED_TOPIC));
		OrderCreatedMessage orderCreated = null;
		try {
			orderCreated = objectMapper.readValue(message, OrderCreatedMessage.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		Optional<InventoryEntity> inventoryOptional = inventoryRepository.findByProduct(orderCreated.getProduct());
		if (inventoryOptional.isPresent()) {
			InventoryEntity inventoryEntity = inventoryOptional.get();
			if (inventoryEntity.getAmount() > orderCreated.amount) {
				int amount = inventoryEntity.getAmount() - orderCreated.amount;
				inventoryEntity.setAmount(amount);
				inventoryRepository.save(inventoryEntity);
				PaymentRequestedMessage paymentRequestedMessage = PaymentRequestedMessage.builder()
						.orderId(orderCreated.getOrderId())
						.money(orderCreated.amount * inventoryEntity.getPrice())
						.build();
				paymentRequestedProducer.sendMessage(paymentRequestedMessage);
				log.info(MessageFormat.format("Send payment request message {0} to topic {1}", paymentRequestedMessage, PAYMENT_REQUESTED_TOPIC));
			} else {
				log.info(MessageFormat.format("Required {0} from product {1}, only have {2}", orderCreated.amount, orderCreated.product, inventoryEntity.getAmount()));
				OrderRejectedMessage orderRejectedMessage = OrderRejectedMessage.builder()
						.orderId(orderCreated.getOrderId())
						.reason("Product does not have enough")
						.build();
				orderRejectedProducer.sendMessage(orderRejectedMessage);
			}
		} else {
			log.info("Could not find product {} in message {}", orderCreated.product, message);
			OrderRejectedMessage orderRejectedMessage = OrderRejectedMessage.builder()
					.orderId(orderCreated.getOrderId())
					.reason("Product not found")
					.build();
			orderRejectedProducer.sendMessage(orderRejectedMessage);
		}
	}

	@KafkaListener(topics = ORDER_REJECTED_TOPIC)
	public void consumePaymentRejected(String message) {
	}

	@Data
	@NoArgsConstructor
	static private class OrderCreatedMessage {
		String orderId;
		String product;
		Integer amount;
		String username;
	}
}
