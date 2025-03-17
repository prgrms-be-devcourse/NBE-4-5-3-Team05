package com.NBE_4_5_2.Team5.global.exception.payment;

import com.NBE_4_5_2.Team5.global.exception.ServiceException;

public class PaymentChargeException extends ServiceException {
    public PaymentChargeException(String code, String message) {
        super(code, message);
    }
}
