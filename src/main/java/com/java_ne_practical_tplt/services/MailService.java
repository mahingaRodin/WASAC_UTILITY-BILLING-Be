package com.java_ne_practical_tplt.services;

import com.java_ne_practical_tplt.payloads.enums.EEmailTemplateType;

import java.util.Map;

public interface MailService {
    void sendTemplateEmail(String to, EEmailTemplateType templateType, Map<String, Object> variables);
}
