package pl.bpiatek.linkshortenernotificationservice.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import java.time.Clock;

@Configuration
class EmailConfig {

    @Bean
    EmailSender emailSender(@Value("${app.mail.from}") String fromEmail, JavaMailSender mailSender) {
        return new SmtpEmailSender(mailSender, fromEmail);
    }

    @Bean
    UserLifecycleKafkaConsumer userLifecycleKafkaConsumer(NotificationService notificationService) {
        return new UserLifecycleKafkaConsumer(notificationService);
    }

    @Bean
    NotificationLogRepository notificationLogRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcNotificationLogRepository(jdbcTemplate);
    }

    @Bean
    NotificationService notificationService(NotificationLogRepository logRepository,
                                            EmailSender emailSender,
                                            TemplateEngine templateEngine,
                                            Clock clock) {
        return new NotificationService(logRepository, emailSender, templateEngine, clock);
    }
}
