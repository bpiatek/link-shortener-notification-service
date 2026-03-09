package pl.bpiatek.linkshortenernotificationservice.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestClient;
import org.thymeleaf.TemplateEngine;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;

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
    NotificationService notificationService(
                                            EmailSender emailSender,
                                            TemplateEngine templateEngine,
                                            NotificationLogService notificationLogService) {
        return new NotificationService(emailSender, templateEngine, notificationLogService);
    }

    @Bean
    NotificationLogService notificationLogService(NotificationLogRepository notificationLogRepository, Clock clock) {
        return new NotificationLogService(notificationLogRepository, clock);
    }

    @Bean
    RestClient vaultRestClient(RestClient.Builder builder,
                               @Value("${vault.address:http://vault.vault.svc.cluster.local:8200}") String vaultAddress) {
        var httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        return builder
                .baseUrl(vaultAddress)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {})
                .build();
    }
}
