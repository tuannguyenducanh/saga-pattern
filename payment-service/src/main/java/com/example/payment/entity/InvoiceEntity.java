package com.example.payment.entity;

import com.example.payment.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "inventory")
public class InvoiceEntity {

	@Id
	@Column(name = "ID")
	private String id;

	@Column(name = "ORDER_ID")
	private String orderId;

	@Column(name = "PRODUCT")
	private String product;

	@Column(name = "AMOUNT")
	private int amount;

	@Column(name = "PRICE")
	private int price;

	@Column(name = "TOTAL")
	private Integer total;

	@Column(name = "USERNAME")
	private String username;

	@Column(name = "ADDRESS")
	private String address;

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private InvoiceStatus status;
}
