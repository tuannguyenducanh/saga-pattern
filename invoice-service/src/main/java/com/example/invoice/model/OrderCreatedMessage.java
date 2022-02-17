package com.example.invoice.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OrderCreatedMessage {

	String orderId;
	String product;
	Integer amount;
	String username;
	String address;
}