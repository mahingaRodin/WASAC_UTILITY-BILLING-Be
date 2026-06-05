package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.Payment;
import com.wasac.utilitybilling.dto.PaymentRequest;

public interface PaymentService {
    Payment process(PaymentRequest request);
}
