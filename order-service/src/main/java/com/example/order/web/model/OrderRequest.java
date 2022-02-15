package com.example.order.web.model;

import lombok.Data;

@Data
public class OrderRequest {

	private String product;
	private int amount;
	private String username;
	private String address;

}
