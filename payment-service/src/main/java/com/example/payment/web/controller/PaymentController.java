package com.example.payment.web.controller;

import com.example.payment.entity.PaymentEntity;
import com.example.payment.service.PaymentService;
import com.example.payment.web.model.PaymentRequest;
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
	public ResponseEntity updateInvoice(@RequestBody PaymentRequest paymentRequest) {
		PaymentEntity paymentEntity = paymentService.updatePaymentStatus(paymentRequest);
		return ResponseEntity.ok(paymentEntity);
	}
}
