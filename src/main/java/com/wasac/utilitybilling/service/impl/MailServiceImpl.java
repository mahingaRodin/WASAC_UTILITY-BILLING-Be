package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.config.AppMailProperties;
import com.wasac.utilitybilling.service.EmailTemplateService;
import com.wasac.utilitybilling.service.MailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;
    private final AppMailProperties appMailProperties;

    @Override
    public void sendTemplateEmail(String to, String templateName, Map<String, Object> variables) {
        try {
            String body = emailTemplateService.render(templateName, variables);
            String subject = emailTemplateService.subject(templateName, variables);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom(appMailProperties.getFrom(), appMailProperties.getSenderName());
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Email send failed for {}", to, ex);
            throw new RuntimeException("Failed to send email");
        }
    }

    @Override
    public void sendTemplateEmailWithAttachment(
            String to,
            String templateName,
            Map<String, Object> variables,
            String attachmentFilename,
            byte[] attachment
    ) {
        try {
            String body = emailTemplateService.render(templateName, variables);
            String subject = emailTemplateService.subject(templateName, variables);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom(appMailProperties.getFrom(), appMailProperties.getSenderName());
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.addAttachment(attachmentFilename, new ByteArrayResource(attachment), "application/pdf");
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Email send with attachment failed for {}", to, ex);
            throw new RuntimeException("Failed to send email");
        }
    }
}
