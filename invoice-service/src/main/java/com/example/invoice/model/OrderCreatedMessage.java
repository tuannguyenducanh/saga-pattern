package com.example.invoice.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderCreatedMessage {

	String orderId;
	String product;
	Integer amount;
	String username;
	String address;
}