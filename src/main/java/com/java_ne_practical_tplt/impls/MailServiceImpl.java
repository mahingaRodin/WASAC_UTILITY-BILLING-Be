package com.java_ne_practical_tplt.impls;

import com.java_ne_practical_tplt.configs.AppMailProperties;
import com.java_ne_practical_tplt.payloads.enums.EEmailTemplateType;
import com.java_ne_practical_tplt.services.EmailTemplateService;
import com.java_ne_practical_tplt.services.MailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void sendTemplateEmail(String to, EEmailTemplateType templateType, Map<String, Object> variables) {
        try {
            String htmlBody = emailTemplateService.render(templateType, variables);
            String subject = emailTemplateService.subject(templateType, variables);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom(appMailProperties.getFrom(), appMailProperties.getSenderName());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Sent {} email to {}", templateType.name(), to);
        } catch (Exception ex) {
            log.error("Failed to send {} email to {}", templateType.name(), to, ex);
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }
}
