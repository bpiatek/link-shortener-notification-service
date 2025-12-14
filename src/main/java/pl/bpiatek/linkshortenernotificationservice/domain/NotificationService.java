package pl.bpiatek.linkshortenernotificationservice.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.bpiatek.contracts.user.UserLifecycleEventProto.PasswordResetRequested;
import pl.bpiatek.contracts.user.UserLifecycleEventProto.UserRegistered;

import java.util.Map;

class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private static final String TYPE_WELCOME_EMAIL = "WELCOME_EMAIL";
    private static final String TYPE_PASSWORD_RESET = "PASSWORD_RESET_EMAIL";

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SENT = "SENT";
    private static final String STATUS_FAILED = "FAILED";

    private final EmailSender emailSender;
    private final TemplateEngine templateEngine;
    private final NotificationLogService notificationLogService;

    public NotificationService(EmailSender emailSender, TemplateEngine templateEngine, NotificationLogService notificationLogService) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
        this.notificationLogService = notificationLogService;
    }

    void processUserRegistration(String eventId, UserRegistered payload) {
        var variables = Map.of("verificationUrl", (Object) payload.getVerificationUrl());

        var request = new EmailRequest(
                eventId,
                payload.getEmail(),
                TYPE_WELCOME_EMAIL,
                "Welcome to Link Shortener!",
                "welcome-email",
                variables
        );

        processNotification(request);
    }

    void processPasswordResetRequest(String eventId, PasswordResetRequested payload) {
        var variables = Map.of(
                "email", payload.getEmail(),
                "resetUrl", (Object) payload.getResetUrl()
        );

        var request = new EmailRequest(
                eventId,
                payload.getEmail(),
                TYPE_PASSWORD_RESET,
                "Reset Your Password - Link Shortener",
                "password-reset-email",
                variables
        );

        processNotification(request);
    }


    private void processNotification(EmailRequest req) {
        if (notificationLogService.existsByEventId(req.eventId())) {
            log.warn("Notification for event ID '{}' has already been processed. Skipping.", req.eventId());
            return;
        }

        if (!trySavePendingLog(req)) {
            return;
        }

        var context = new Context();
        context.setVariables(req.variables());
        var htmlBody = templateEngine.process(req.templateName(), context);


        sendEmailAndUpdateStatus(req, htmlBody);
    }

    private boolean trySavePendingLog(EmailRequest req) {
        try {
            notificationLogService.saveLog(req.eventId(), req.recipient(), req.type(), STATUS_PENDING, null);
            return true;
        } catch (Exception e) {
            log.warn("Could not save PENDING log for event '{}'. Assuming duplicate or concurrent processing.", req.eventId());
            return false;
        }
    }

    private void sendEmailAndUpdateStatus(EmailRequest req, String htmlBody) {
        try {
            emailSender.send(req.recipient(), req.subject(), htmlBody);
            notificationLogService.updateLogStatus(req.eventId(), STATUS_SENT, null);
        } catch (Exception e) {
            log.error("Failed to send email for event ID '{}'. Reason: {}", req.eventId(), e.getMessage());
            notificationLogService.updateLogStatus(req.eventId(), STATUS_FAILED, e.getMessage());
        }
    }

    private record EmailRequest(
            String eventId,
            String recipient,
            String type,
            String subject,
            String templateName,
            Map<String, Object> variables
    ) {}
}