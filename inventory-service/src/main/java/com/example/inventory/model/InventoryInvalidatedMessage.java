package com.example.inventory.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class InventoryInvalidatedMessage {

	private String orderId;
	private String reason;

}
