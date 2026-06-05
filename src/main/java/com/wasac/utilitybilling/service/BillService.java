package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.Bill;
import com.wasac.utilitybilling.dto.BillRequest;

public interface BillService {
    Bill create(BillRequest request);
}
