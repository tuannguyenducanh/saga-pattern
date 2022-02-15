package com.example.payment.service;

import com.example.payment.entity.InvoiceEntity;
import com.example.payment.enums.InvoiceStatus;
import com.example.payment.kafka.PaymentApprovedProducer;
import com.example.payment.kafka.PaymentRejectedProducer;
import com.example.payment.model.PaymentRejectedMessage;
import com.example.payment.model.PaymentRequestedMessage;
import com.example.payment.repository.InvoiceRepository;
import com.example.payment.web.model.InvoiceRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class InvoiceService {

	private static final String INVENTORY_PROCESSED_TOPIC = "inventory.processed";

	private final ObjectMapper objectMapper;
	private final PaymentApprovedProducer paymentApprovedProducer;
	private final PaymentRejectedProducer paymentRejectedProducer;
	private final InvoiceRepository invoiceRepository;

	public InvoiceService(ObjectMapper objectMapper,
						  PaymentApprovedProducer paymentApprovedProducer,
						  PaymentRejectedProducer paymentRejectedProducer,
						  InvoiceRepository invoiceRepository) {
		this.objectMapper = objectMapper;
		this.paymentApprovedProducer = paymentApprovedProducer;
		this.paymentRejectedProducer = paymentRejectedProducer;
		this.invoiceRepository = invoiceRepository;
	}

	@KafkaListener(groupId = "com.example.payment", topics = INVENTORY_PROCESSED_TOPIC)
	public void consume(String message) {
		PaymentRequestedMessage paymentRequestedMessage = null;
		try {
			paymentRequestedMessage = objectMapper.readValue(message, PaymentRequestedMessage.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		InvoiceEntity invoiceEntity = InvoiceEntity.builder()
				.id(UUID.randomUUID().toString())
				.amount(paymentRequestedMessage.getAmount())
				.product(paymentRequestedMessage.getProduct())
				.price(paymentRequestedMessage.getMoney())
				.total(paymentRequestedMessage.getMoney())
				.address(paymentRequestedMessage.getAddress())
				.status(InvoiceStatus.PENDING)
				.build();
		invoiceRepository.save(invoiceEntity);
	}

	public InvoiceEntity updateInvoiceStatus(InvoiceRequest invoiceRequest) {
		Optional<InvoiceEntity> invoiceEntityOptional = invoiceRepository.findById(invoiceRequest.getInvoiceId());
		if (invoiceEntityOptional.isPresent()) {
			InvoiceEntity invoiceEntity = invoiceEntityOptional.get();
			if (invoiceRequest.isSuccess()) {
				invoiceEntity.setStatus(InvoiceStatus.FINISHED);
				invoiceRepository.save(invoiceEntity);
				paymentApprovedProducer.sendMessage(invoiceEntity);
			} else {
				invoiceEntity.setStatus(InvoiceStatus.FAILED);
				invoiceRepository.save(invoiceEntity);
				paymentRejectedProducer.sendMessage(invoiceEntity);
			}
		}
		return null;
	}
}
