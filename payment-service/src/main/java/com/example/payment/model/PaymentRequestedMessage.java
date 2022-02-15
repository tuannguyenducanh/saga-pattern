package com.example.payment.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentRequestedMessage {

	private String orderId;
	private int money;
	private String product;
	private Integer amount;
	private String username;
	private String address;

}
