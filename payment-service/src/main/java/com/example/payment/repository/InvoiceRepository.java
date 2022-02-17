package com.example.payment.repository;

import com.example.payment.entity.PaymentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends CrudRepository<PaymentEntity, String> {
}
