package com.wasac.utilitybilling.service;

import java.util.Map;

public interface EmailTemplateService {
    String render(String templateName, Map<String, Object> variables);
    String subject(String templateName, Map<String, Object> variables);
}
