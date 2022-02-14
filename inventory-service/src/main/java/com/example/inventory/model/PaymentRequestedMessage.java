package com.example.inventory.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentRequestedMessage {

	private String orderId;
	private int money;

}
