package com.example.payment.kafka;

import com.example.payment.entity.InvoiceEntity;
import com.example.payment.model.PaymentApprovedMessage;
import com.example.payment.model.PaymentRequestedMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentApprovedProducer {

	private static final String TOPIC = "payment.approved";

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public PaymentApprovedProducer(KafkaTemplate<String, String> kafkaTemplate,
								ObjectMapper objectMapper) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}

	public void sendMessage(InvoiceEntity invoiceEntity) {
		PaymentApprovedMessage approvedMessage = PaymentApprovedMessage.builder()
				.orderId(invoiceEntity.getOrderId())
				.product(invoiceEntity.getProduct())
				.amount(invoiceEntity.getAmount())
				.username(invoiceEntity.getUsername())
				.address(invoiceEntity.getAddress())
				.build();
		log.info("Invoice approved : " + invoiceEntity);
		try {
			kafkaTemplate.send(TOPIC, objectMapper.writeValueAsString(approvedMessage));
		} catch (JsonProcessingException e) {
			log.error(approvedMessage + " cannot parse to json");
		}
	}

}
