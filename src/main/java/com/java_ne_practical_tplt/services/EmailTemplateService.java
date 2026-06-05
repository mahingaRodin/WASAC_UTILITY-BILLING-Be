package com.java_ne_practical_tplt.services;

import com.java_ne_practical_tplt.payloads.enums.EEmailTemplateType;

import java.util.Map;

public interface EmailTemplateService {
    String render(EEmailTemplateType templateType, Map<String, Object> variables);
    String subject(EEmailTemplateType templateType, Map<String, Object> variables);
}
