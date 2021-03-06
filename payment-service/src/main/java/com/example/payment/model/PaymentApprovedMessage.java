package com.example.payment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentApprovedMessage {

	String orderId;
	String product;
	Integer amount;
	String username;
	String address;

}
