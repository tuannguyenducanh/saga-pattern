package com.example.order.exception;

public class OrderNotFoundException extends RuntimeException {

	public OrderNotFoundException(String orderId) {
		super(orderId + " not found");
	}
}
