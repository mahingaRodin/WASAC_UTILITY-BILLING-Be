package com.java_ne_practical_tplt.impls;

import com.java_ne_practical_tplt.payloads.enums.EEmailTemplateType;
import com.java_ne_practical_tplt.services.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {
    private final TemplateEngine templateEngine;

    @Override
    public String render(EEmailTemplateType templateType, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateType.getTemplateName(), context);
    }

    @Override
    public String subject(EEmailTemplateType templateType, Map<String, Object> variables) {
        Object customSubject = variables.get("subject");
        if (customSubject != null) {
            return customSubject.toString();
        }
        return templateType.getDefaultSubject();
    }
}
