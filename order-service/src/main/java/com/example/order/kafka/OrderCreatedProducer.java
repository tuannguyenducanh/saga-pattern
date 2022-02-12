package com.example.order.kafka;

import com.example.order.entity.OrderEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderCreatedProducer {

	private static final String TOPIC = "order.created";

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public OrderCreatedProducer(KafkaTemplate kafkaTemplate,
								ObjectMapper objectMapper) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}

	public void sendMessage(OrderEntity orderEntity) {
		OrderMessage orderMessage = OrderMessage.builder()
				.messageId(orderEntity.getUuid())
				.product(orderEntity.getProduct())
				.amount(orderEntity.getAmount())
				.username(orderEntity.getUsername())
				.build();
		log.info("Order create message: " + orderMessage);
		try {
			kafkaTemplate.send(TOPIC, objectMapper.writeValueAsString(orderMessage));
		} catch (JsonProcessingException e) {
			log.error(orderMessage + " cannot parse to json");
		}
	}

	@Builder
	@Data
	private static class OrderMessage {

		String messageId;
		String product;
		Integer amount;
		String username;

	}
}
