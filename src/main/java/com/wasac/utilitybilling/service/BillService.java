package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.BillRequest;
import com.wasac.utilitybilling.dto.BillResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BillService {
    BillResponse create(BillRequest request);
    Page<BillResponse> list(Pageable pageable);
    Page<BillResponse> listPendingApproval(Pageable pageable);
    BillResponse getById(UUID id);
    BillResponse getByReference(String billReference);
    Page<BillResponse> getByCustomerId(UUID customerId, Pageable pageable);
    Page<BillResponse> getMyBills(Pageable pageable);
    BillResponse approve(UUID id);
    BillResponse reject(UUID id);
    byte[] generateBillPdf(UUID id);
    byte[] generateReceiptPdf(UUID id);
}
