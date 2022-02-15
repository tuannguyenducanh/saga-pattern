package com.example.payment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRejectedMessage {

	private String orderId;
	private String reason;
	String product;
	Integer amount;

}
