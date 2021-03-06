package com.example.inventory.kafka;

import com.example.inventory.model.InventoryProcessedMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InventoryProcessedProducer {

	public static final String INVENTORY_PROCESSED_TOPIC = "inventory.processed";

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public InventoryProcessedProducer(KafkaTemplate kafkaTemplate,
									  ObjectMapper objectMapper) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}

	public void sendMessage(InventoryProcessedMessage message) {
		log.info("Sending inventory processed message: " + message);
		try {
			kafkaTemplate.send(INVENTORY_PROCESSED_TOPIC, objectMapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			log.error(message + " cannot parse to json");
		}
	}
}
