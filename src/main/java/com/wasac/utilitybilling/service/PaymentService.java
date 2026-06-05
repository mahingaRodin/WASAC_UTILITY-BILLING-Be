package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.PaymentRequest;
import com.wasac.utilitybilling.dto.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    PaymentResponse process(PaymentRequest request);
    Page<PaymentResponse> list(Pageable pageable);
    Page<PaymentResponse> listPendingApproval(Pageable pageable);
    PaymentResponse getById(UUID id);
    List<PaymentResponse> getByBillReference(String billReference);
    Page<PaymentResponse> getMyPayments(Pageable pageable);
    PaymentResponse approve(UUID id);
    PaymentResponse reject(UUID id);
}
