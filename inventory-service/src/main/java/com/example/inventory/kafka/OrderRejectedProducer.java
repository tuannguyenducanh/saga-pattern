package com.example.inventory.kafka;

import com.example.inventory.model.OrderRejectedMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderRejectedProducer {

	private static final String ORDER_REJECTED_TOPIC = "order.rejected";

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public OrderRejectedProducer(KafkaTemplate kafkaTemplate,
								ObjectMapper objectMapper) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}

	public void sendMessage(OrderRejectedMessage message) {
		log.info("Sending order rejected message: " + message);
		try {
			kafkaTemplate.send(ORDER_REJECTED_TOPIC, objectMapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			log.error(message + " cannot parse to json");
		}
	}
}
