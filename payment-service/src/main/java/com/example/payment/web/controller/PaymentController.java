package com.example.payment.web.controller;

import com.example.payment.entity.PaymentEntity;
import com.example.payment.service.PaymentService;
import com.example.payment.web.model.InvoiceRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping
	public ResponseEntity updateInvoice(@RequestBody InvoiceRequest invoiceRequest) {
		PaymentEntity paymentEntity = paymentService.updatePaymentStatus(invoiceRequest);
		return ResponseEntity.ok(paymentEntity);
	}
}
