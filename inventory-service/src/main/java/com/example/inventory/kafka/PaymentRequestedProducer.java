package com.example.inventory.kafka;

import com.example.inventory.model.PaymentRequestedMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentRequestedProducer {

	public static final String PAYMENT_REQUESTED_TOPIC = "payment.requested";

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public PaymentRequestedProducer(KafkaTemplate kafkaTemplate,
								 ObjectMapper objectMapper) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}

	public void sendMessage(PaymentRequestedMessage message) {
		log.info("Sending order rejected message: " + message);
		try {
			kafkaTemplate.send(PAYMENT_REQUESTED_TOPIC, objectMapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			log.error(message + " cannot parse to json");
		}
	}
}
