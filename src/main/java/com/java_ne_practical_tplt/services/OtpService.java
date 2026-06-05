package com.java_ne_practical_tplt.services;

import com.java_ne_practical_tplt.models.User;
import com.java_ne_practical_tplt.payloads.enums.EOtpPurpose;

public interface OtpService {
    String generateAndSendOtp(User user, EOtpPurpose purpose);
    void validateOtp(User user, String otpCode, EOtpPurpose purpose);
}
