package com.example.payment.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@NoArgsConstructor
public class InventoryProcessedMessage {

	private String orderId;
	private int money;
	private String product;
	private Integer amount;
	private String username;
	private String address;

}
