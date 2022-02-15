package com.example.payment.kafka;

import com.example.payment.entity.InvoiceEntity;
import com.example.payment.model.PaymentRejectedMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentRejectedProducer {

	private static final String TOPIC = "payment.rejected";

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public PaymentRejectedProducer(KafkaTemplate<String, String> kafkaTemplate,
								   ObjectMapper objectMapper) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}

	public void sendMessage(InvoiceEntity message) {
		PaymentRejectedMessage paymentRejectedMessage = PaymentRejectedMessage
				.builder()
				.build();
		log.info("Payment rejected prepared to send: {}", message);
		try {
			kafkaTemplate.send(TOPIC, objectMapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			log.error(message + " cannot parse to json");
		}
	}

}
