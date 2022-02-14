package com.example.inventory.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OrderRejectedMessage {

	private String orderId;
	private String reason;

}
