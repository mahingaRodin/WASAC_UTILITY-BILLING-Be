package com.wasac.utilitybilling.service.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.wasac.utilitybilling.domain.enums.PaymentMethod;
import com.wasac.utilitybilling.dto.BillLineItemResponse;
import com.wasac.utilitybilling.dto.BillResponse;
import com.wasac.utilitybilling.dto.PaymentResponse;
import com.wasac.utilitybilling.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PdfServiceImpl implements PdfService {
    private final TemplateEngine templateEngine;

    public PdfServiceImpl() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/pdf/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
    }

    @Override
    public byte[] generateBillPdf(BillResponse bill) {
        Map<String, Object> vars = new HashMap<>();
        List<BillLineItemResponse> items = bill.getLineItems() == null ? List.of() : bill.getLineItems();
        BigDecimal lineItemsTotal = items.stream()
                .map(BillLineItemResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        vars.put("bill", bill);
        vars.put("lineItems", items);
        vars.put("lineItemsTotal", lineItemsTotal);
        vars.put("amountDue", safeAmount(bill.getAmountDue()));
        vars.put("paidAmount", safeAmount(bill.getPaidAmount()));
        vars.put("outstandingBalance", safeAmount(bill.getOutstandingBalance()));
        return renderToPdf("bill-document", vars);
    }

    @Override
    public byte[] generateReceiptPdf(BillResponse bill, PaymentResponse payment) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("bill", bill);
        vars.put("payment", payment);
        vars.put("amountPaid", payment != null && payment.getAmountPaid() != null
                ? payment.getAmountPaid()
                : safeAmount(bill.getAmountDue()));
        vars.put("paymentDate", payment != null ? payment.getPaymentDate() : null);
        vars.put("paymentMethod", formatPaymentMethod(payment != null ? payment.getPaymentMethod() : null));
        vars.put("receiptNumber", payment != null && payment.getId() != null
                ? payment.getId()
                : bill.getBillReference());
        return renderToPdf("payment-receipt", vars);
    }

    private byte[] renderToPdf(String templateName, Map<String, Object> variables) {
        try {
            String html = render(templateName, variables);
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                PdfRendererBuilder b = new PdfRendererBuilder();
                b.useFastMode();
                b.withHtmlContent(html, null);
                b.toStream(os);
                b.run();
                return os.toByteArray();
            }
        } catch (Exception ex) {
            log.error("Failed to generate PDF for template {}", templateName, ex);
            throw new RuntimeException("Failed to generate PDF");
        }
    }

    private String render(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String formatPaymentMethod(PaymentMethod method) {
        if (method == null) {
            return "N/A";
        }
        return switch (method) {
            case CASH -> "Cash";
            case BANK_TRANSFER -> "Bank Transfer";
            case MOBILE_MONEY -> "Mobile Money";
            case CARD -> "Card";
        };
    }
}
