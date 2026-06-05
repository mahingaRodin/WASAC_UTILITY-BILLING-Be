package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.User;
import com.wasac.utilitybilling.domain.enums.OtpPurpose;

public interface OtpService {
    void generateAndSendOtp(User user, OtpPurpose purpose);
    void validateOtp(User user, String otp, OtpPurpose purpose);
}
