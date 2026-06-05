package com.wasac.utilitybilling.service;

import java.util.Map;

public interface MailService {
    void sendTemplateEmail(String to, String templateName, Map<String, Object> variables);
    void sendTemplateEmailWithAttachment(
            String to,
            String templateName,
            Map<String, Object> variables,
            String attachmentFilename,
            byte[] attachment
    );
}
