package com.example.invoice.service;

import com.example.invoice.entity.InvoiceEntity;
import com.example.invoice.entity.OrderMessageEntity;
import com.example.invoice.kafka.InvoiceApprovedProducer;
import com.example.invoice.model.OrderCreatedMessage;
import com.example.invoice.model.PaymentApprovedMessage;
import com.example.invoice.repository.InvoiceRepository;
import com.example.invoice.repository.OrderMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.example.invoice.enums.InvoiceStatus.APPROVED;

@Service
public class InvoiceService {

	private static final String ORDER_CREATED_TOPIC = "order.created";
	private static final String PAYMENT_APPROVED_TOPIC = "payment.approved";

	private final ObjectMapper objectMapper;
	private final InvoiceRepository invoiceRepository;
	private final OrderMessageRepository orderMessageRepository;
	private final InvoiceApprovedProducer invoiceApprovedProducer;

	public InvoiceService(ObjectMapper objectMapper,
						  InvoiceRepository invoiceRepository,
						  OrderMessageRepository orderMessageRepository,
						  InvoiceApprovedProducer invoiceApprovedProducer) {
		this.objectMapper = objectMapper;
		this.invoiceRepository = invoiceRepository;
		this.orderMessageRepository = orderMessageRepository;
		this.invoiceApprovedProducer = invoiceApprovedProducer;
	}

	@KafkaListener(topics = ORDER_CREATED_TOPIC)
	public void consumeOrderCreated(String message) {
		try {
			OrderCreatedMessage orderCreatedMessage = objectMapper.readValue(message, OrderCreatedMessage.class);
			OrderMessageEntity orderMessageEntity = OrderMessageEntity.builder()
					.id(orderCreatedMessage.getOrderId())
					.product(orderCreatedMessage.getProduct())
					.amount(orderCreatedMessage.getAmount())
					.username(orderCreatedMessage.getUsername())
					.address(orderCreatedMessage.getAddress())
					.orderId(orderCreatedMessage.getOrderId())
					.build();
			orderMessageRepository.save(orderMessageEntity);
		} catch (JsonProcessingException e) {
			// handle message cannot parse
		}
	}

	@KafkaListener(topics = PAYMENT_APPROVED_TOPIC)
	public void consumePaymentApproved(String message) {
		try {
			PaymentApprovedMessage approvedMessage = objectMapper.readValue(message, PaymentApprovedMessage.class);
			orderMessageRepository.findById(approvedMessage.getOrderId())
					.ifPresentOrElse(entity -> {
						InvoiceEntity invoiceEntity = InvoiceEntity.builder()
								.id(UUID.randomUUID().toString())
								.product(approvedMessage.getProduct())
								.amount(approvedMessage.getAmount())
								.username(approvedMessage.getUsername())
								.address(approvedMessage.getAddress())
								.orderId(approvedMessage.getOrderId())
								.status(APPROVED)
								.build();
						invoiceRepository.save(invoiceEntity);
						invoiceApprovedProducer.sendMessage(approvedMessage.getOrderId());
					}, () -> {
						// handle case when payment come before order
					});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
