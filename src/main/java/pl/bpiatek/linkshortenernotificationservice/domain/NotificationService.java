package pl.bpiatek.linkshortenernotificationservice.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.bpiatek.contracts.user.UserLifecycleEventProto.UserRegistered;

import java.time.Clock;

class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final String NOTIFICATION_TYPE = "WELCOME_EMAIL";

    private final NotificationLogRepository logRepository;
    private final EmailSender emailSender;
    private final TemplateEngine templateEngine;
    private final Clock clock;
    private final String appBaseUrl;

    NotificationService(NotificationLogRepository logRepository, EmailSender emailSender, TemplateEngine templateEngine, Clock clock, String appBaseUrl) {
        this.logRepository = logRepository;
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
        this.clock = clock;
        this.appBaseUrl = appBaseUrl;
    }

    @Transactional
    void processUserRegistration(String eventId, UserRegistered payload) {
        if (logRepository.existsByEventId(eventId)) {
            log.warn("Notification for event ID '{}' has already been processed. Skipping.", eventId);
            return;
        }

        var verificationUrl = String.format("%s/auth/verify-email?token=%s", appBaseUrl, payload.getVerificationToken());
        var subject = "Welcome to Link Shortener!";

        var context = new Context();
        context.setVariable("verificationUrl", verificationUrl);
        var htmlBody = templateEngine.process("welcome-email", context);

        try {
            emailSender.send(payload.getEmail(), subject, htmlBody);
            saveLog(eventId, payload.getEmail(), "SENT", null);
        } catch (Exception e) {
            log.error("Failed to send welcome email for event ID '{}'. Reason: {}", eventId, e.getMessage());
            saveLog(eventId, payload.getEmail(), "FAILED", e.getMessage());
        }
    }

    private void saveLog(String eventId, String email, String status, String errorMessage) {
        var logEntry = new NotificationLog(
                null,
                eventId,
                email,
                NOTIFICATION_TYPE,
                status,
                clock.instant(),
                errorMessage
        );
        logRepository.save(logEntry);
    }
}
