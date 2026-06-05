package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.BillResponse;
import com.wasac.utilitybilling.dto.PaymentResponse;

public interface PdfService {
    byte[] generateBillPdf(BillResponse bill);
    byte[] generateReceiptPdf(BillResponse bill, PaymentResponse payment);
}
