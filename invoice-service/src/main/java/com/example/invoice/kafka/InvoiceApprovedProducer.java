package com.example.invoice.kafka;

import com.example.invoice.model.InvoiceApprovedMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InvoiceApprovedProducer {

	private static final String INVOICE_APPROVED_TOPIC = "invoice.approved";

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public InvoiceApprovedProducer(KafkaTemplate<String, String> kafkaTemplate,
								ObjectMapper objectMapper) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}

	public void sendMessage(String orderId) {
		InvoiceApprovedMessage orderMessage = InvoiceApprovedMessage.builder()
				.orderId(orderId)
				.build();
		log.info("Order create message: " + orderMessage);
		try {
			kafkaTemplate.send(INVOICE_APPROVED_TOPIC, objectMapper.writeValueAsString(orderMessage));
		} catch (JsonProcessingException e) {
			log.error(orderMessage + " cannot parse to json");
		}
	}

}
