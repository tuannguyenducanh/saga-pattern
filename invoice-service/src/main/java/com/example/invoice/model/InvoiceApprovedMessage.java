package com.example.invoice.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceApprovedMessage {

	private String orderId;

}
