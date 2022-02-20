package com.example.inventory.service;

import com.example.inventory.entity.InventoryEntity;
import com.example.inventory.kafka.InventoryInvalidatedProducer;
import com.example.inventory.kafka.InventoryProcessedProducer;
import com.example.inventory.model.InventoryInvalidatedMessage;
import com.example.inventory.model.InventoryProcessedMessage;
import com.example.inventory.repository.InventoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.example.inventory.kafka.InventoryProcessedProducer.INVENTORY_PROCESSED_TOPIC;

@Slf4j
@Service
public class InventoryService {

	private static final String ORDER_CREATED_TOPIC = "order.created";
	private static final String PAYMENT_REJECTED_TOPIC = "payment.rejected";

	private final ObjectMapper objectMapper;
	private final InventoryRepository inventoryRepository;
	private final InventoryInvalidatedProducer inventoryInvalidatedProducer;
	private final InventoryProcessedProducer inventoryProcessedProducer;

	public InventoryService(InventoryRepository inventoryRepository,
						    ObjectMapper objectMapper,
							InventoryInvalidatedProducer inventoryInvalidatedProducer,
							InventoryProcessedProducer inventoryProcessedProducer) {
		this.objectMapper = objectMapper;
		this.inventoryRepository = inventoryRepository;
		this.inventoryInvalidatedProducer = inventoryInvalidatedProducer;
		this.inventoryProcessedProducer = inventoryProcessedProducer;
	}

	@KafkaListener(topics = ORDER_CREATED_TOPIC)
	public void comsumeOrderCreated(String message) throws JsonProcessingException {
		log.info("Consumer message {} from topic {}", message, ORDER_CREATED_TOPIC);
		OrderCreatedMessage orderCreated = objectMapper.readValue(message, OrderCreatedMessage.class);
		Optional<InventoryEntity> inventoryOptional = inventoryRepository.findByProduct(orderCreated.getProduct());
		if (inventoryOptional.isPresent()) {
			InventoryEntity inventoryEntity = inventoryOptional.get();
			if (inventoryEntity.getAmount() > orderCreated.amount) {
				int amount = inventoryEntity.getAmount() - orderCreated.amount;
				inventoryEntity.setAmount(amount);
				inventoryRepository.save(inventoryEntity);
				InventoryProcessedMessage inventoryProcessedMessage = InventoryProcessedMessage.builder()
						.orderId(orderCreated.getOrderId())
						.money(orderCreated.amount * inventoryEntity.getPrice())
						.product(orderCreated.getProduct())
						.amount(orderCreated.getAmount())
						.username(orderCreated.getUsername())
						.address(orderCreated.getAddress())
						.build();
				inventoryProcessedProducer.sendMessage(inventoryProcessedMessage);
				log.info("Sent inventory processed message {} to topic {}", inventoryProcessedMessage, INVENTORY_PROCESSED_TOPIC);
			} else {
				log.info("Required {} from product {}, only have {}", orderCreated.amount, orderCreated.product, inventoryEntity.getAmount());
				InventoryInvalidatedMessage inventoryInvalidatedMessage = InventoryInvalidatedMessage.builder()
						.orderId(orderCreated.getOrderId())
						.reason("Product does not have enough")
						.build();
				inventoryInvalidatedProducer.sendMessage(inventoryInvalidatedMessage);
			}
		} else {
			log.info("Could not finds product {} in message {}", orderCreated.product, message);
			InventoryInvalidatedMessage inventoryInvalidatedMessage = InventoryInvalidatedMessage.builder()
					.orderId(orderCreated.getOrderId())
					.reason("Product not found")
					.build();
			inventoryInvalidatedProducer.sendMessage(inventoryInvalidatedMessage);
		}
	}

	@KafkaListener(topics = PAYMENT_REJECTED_TOPIC)
	public void consumePaymentRejected(String message) throws JsonProcessingException {
		PaymentRejectedMessage paymentRejectedMessage = objectMapper.readValue(message, PaymentRejectedMessage.class);
		inventoryRepository.findByProduct(paymentRejectedMessage.getProduct()).ifPresentOrElse(
				entity -> {
					Integer amount = entity.getAmount() + paymentRejectedMessage.getAmount();
					entity.setAmount(amount);
					inventoryRepository.save(entity);
				},
				() -> {
					log.error("Product {} not found for message {} from {} topic",
							paymentRejectedMessage.getProduct(), paymentRejectedMessage, PAYMENT_REJECTED_TOPIC);
				}
		);
	}

	@Data
	@NoArgsConstructor
	static private class OrderCreatedMessage {
		String orderId;
		String product;
		Integer amount;
		String username;
		String address;
	}

	@Data
	@NoArgsConstructor
	public class PaymentRejectedMessage {

		private String orderId;
		private String reason;
		String product;
		Integer amount;

	}
}
