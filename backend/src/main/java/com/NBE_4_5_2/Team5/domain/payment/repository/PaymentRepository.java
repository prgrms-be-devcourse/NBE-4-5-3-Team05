package com.NBE_4_5_2.Team5.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.NBE_4_5_2.Team5.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, String> {
}
