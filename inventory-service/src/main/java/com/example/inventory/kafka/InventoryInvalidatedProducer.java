package com.example.inventory.kafka;

import com.example.inventory.model.InventoryInvalidatedMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InventoryInvalidatedProducer {

	private static final String INVENTORY_INVALIDATED_TOPIC = "inventory.invalidated";

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public InventoryInvalidatedProducer(KafkaTemplate kafkaTemplate,
										ObjectMapper objectMapper) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}

	public void sendMessage(InventoryInvalidatedMessage message) {
		log.info("Sending order rejected message: " + message);
		try {
			kafkaTemplate.send(INVENTORY_INVALIDATED_TOPIC, objectMapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			log.error(message + " cannot parse to json");
		}
	}
}
