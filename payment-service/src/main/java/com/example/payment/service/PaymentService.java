package com.example.payment.service;

import com.example.payment.entity.PaymentEntity;
import com.example.payment.enums.PaymentStatus;
import com.example.payment.kafka.PaymentApprovedProducer;
import com.example.payment.kafka.PaymentRejectedProducer;
import com.example.payment.model.InventoryProcessedMessage;
import com.example.payment.repository.InvoiceRepository;
import com.example.payment.web.model.PaymentRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class PaymentService {

	private static final String INVENTORY_PROCESSED_TOPIC = "inventory.processed";

	private final ObjectMapper objectMapper;
	private final PaymentApprovedProducer paymentApprovedProducer;
	private final PaymentRejectedProducer paymentRejectedProducer;
	private final InvoiceRepository paymentRepository;

	public PaymentService(ObjectMapper objectMapper,
						  PaymentApprovedProducer paymentApprovedProducer,
						  PaymentRejectedProducer paymentRejectedProducer,
						  InvoiceRepository paymentRepository) {
		this.objectMapper = objectMapper;
		this.paymentApprovedProducer = paymentApprovedProducer;
		this.paymentRejectedProducer = paymentRejectedProducer;
		this.paymentRepository = paymentRepository;
	}

	@KafkaListener(groupId = "com.example.payment", topics = INVENTORY_PROCESSED_TOPIC)
	public void consume(String message) throws JsonProcessingException {
		log.info("Consumer message {} from topic {}", message, INVENTORY_PROCESSED_TOPIC);
		InventoryProcessedMessage inventoryProcessedMessage = objectMapper.readValue(message, InventoryProcessedMessage.class);
		PaymentEntity paymentEntity = PaymentEntity.builder()
				.id(UUID.randomUUID().toString())
				.total(inventoryProcessedMessage.getMoney())
				.status(PaymentStatus.PENDING)
				.orderId(inventoryProcessedMessage.getOrderId())
				.username(inventoryProcessedMessage.getUsername())
				.build();
		paymentRepository.save(paymentEntity);
		log.info("Persist pending payment {}", paymentEntity);
	}

	public PaymentEntity updatePaymentStatus(PaymentRequest paymentRequest) {
		PaymentEntity paymentEntity = null;
		Optional<PaymentEntity> paymentEntityOptional = paymentRepository.findById(paymentRequest.getPaymentId());
		if (paymentEntityOptional.isPresent()) {
			paymentEntity = paymentEntityOptional.get();
			if (paymentRequest.isSuccess()) {
				paymentEntity.setStatus(PaymentStatus.FINISHED);
				paymentRepository.save(paymentEntity);
				paymentApprovedProducer.sendMessage(paymentEntity);
			} else {
				paymentEntity.setStatus(PaymentStatus.FAILED);
				paymentRepository.save(paymentEntity);
				paymentRejectedProducer.sendMessage(paymentEntity);
			}
		}
		return paymentEntity;
	}
}
