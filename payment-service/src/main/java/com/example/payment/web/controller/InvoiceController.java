package com.example.payment.web.controller;

import com.example.payment.entity.InvoiceEntity;
import com.example.payment.service.InvoiceService;
import com.example.payment.web.model.InvoiceRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InvoiceController {

	private final InvoiceService invoiceService;

	public InvoiceController(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	@PostMapping
	public ResponseEntity updateInvoice(@RequestBody InvoiceRequest invoiceRequest) {
		InvoiceEntity invoiceEntity = invoiceService.updateInvoiceStatus(invoiceRequest);
		return ResponseEntity.ok(invoiceEntity);
	}
}
