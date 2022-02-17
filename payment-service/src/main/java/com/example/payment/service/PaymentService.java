package com.example.payment.service;

import com.example.payment.entity.PaymentEntity;
import com.example.payment.enums.PaymentStatus;
import com.example.payment.kafka.PaymentApprovedProducer;
import com.example.payment.kafka.PaymentRejectedProducer;
import com.example.payment.model.InventoryProcessedMessage;
import com.example.payment.repository.InvoiceRepository;
import com.example.payment.web.model.InvoiceRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

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
	public void consume(String message) {
		InventoryProcessedMessage inventoryProcessedMessage = null;
		try {
			inventoryProcessedMessage = objectMapper.readValue(message, InventoryProcessedMessage.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		PaymentEntity paymentEntity = PaymentEntity.builder()
				.id(UUID.randomUUID().toString())
				.total(inventoryProcessedMessage.getMoney())
				.status(PaymentStatus.PENDING)
				.build();
		paymentRepository.save(paymentEntity);
	}

	public PaymentEntity updatePaymentStatus(InvoiceRequest invoiceRequest) {
		Optional<PaymentEntity> invoiceEntityOptional = paymentRepository.findById(invoiceRequest.getInvoiceId());
		if (invoiceEntityOptional.isPresent()) {
			PaymentEntity paymentEntity = invoiceEntityOptional.get();
			if (invoiceRequest.isSuccess()) {
				paymentEntity.setStatus(PaymentStatus.FINISHED);
				paymentRepository.save(paymentEntity);
				paymentApprovedProducer.sendMessage(paymentEntity);
			} else {
				paymentEntity.setStatus(PaymentStatus.FAILED);
				paymentRepository.save(paymentEntity);
				paymentRejectedProducer.sendMessage(paymentEntity);
			}
		}
		return null;
	}
}
