package pl.bpiatek.linkshortenernotificationservice.domain;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import pl.bpiatek.contracts.user.UserLifecycleEventProto;

import java.util.UUID;

import static jakarta.mail.Message.RecipientType.TO;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@WithDatabaseFixtures
class UserLifecycleKafkaConsumerTest implements WithFullInfrastructure {

    @Autowired
    private KafkaTemplate<String, UserLifecycleEventProto.UserLifecycleEvent> kafkaTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NotificationLogFixtures fixtures;

    @Value("${topic.user.lifecycle}")
    private String topicName;

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("testuser", "testpass"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", redpanda::getBootstrapServers);

        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", ServerSetupTest.SMTP::getPort);
        registry.add("spring.mail.username", () -> "testuser");
        registry.add("spring.mail.password", () -> "testpass");
    }

    @AfterEach
    void cleanup() {
        jdbcTemplate.update("DELETE FROM notification_logs");
        greenMail.reset();
    }

    @Test
    void shouldConsumeUserRegisteredEventAndSendEmail() {
        // given
        var eventId = UUID.randomUUID().toString();
        var userEmail = "test-consumer@example.com";
        var verificationUrl = "https://dev.test.pl/auth/verify?token=123";
        var event = createUserRegisteredEvent(eventId, userEmail, verificationUrl);

        // when
        kafkaTemplate.send(topicName, event);

        // then
        assertEmailWasSent(userEmail, verificationUrl);
        assertThat(fixtures.getByEventId(eventId)).isNotNull();
    }


    private UserLifecycleEventProto.UserLifecycleEvent createUserRegisteredEvent(String eventId, String email, String url) {
        var payload = UserLifecycleEventProto.UserRegistered.newBuilder()
                .setUserId("user-1")
                .setEmail(email)
                .setVerificationUrl(url)
                .build();

        return UserLifecycleEventProto.UserLifecycleEvent.newBuilder()
                .setEventId(eventId)
                .setUserRegistered(payload)
                .build();
    }

    private void assertEmailWasSent(String expectedTo, String expectedUrl) {
        try {
            assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();

            var receivedMessages = greenMail.getReceivedMessages();
            assertThat(receivedMessages).hasSize(1);

            var receivedMessage = receivedMessages[0];
            var body = GreenMailUtil.getBody(receivedMessage);

            try (var softly = new AutoCloseableSoftAssertions()) {
                softly.assertThat(receivedMessage.getRecipients(TO)[0].toString()).isEqualTo(expectedTo);
                softly.assertThat(receivedMessage.getSubject()).isEqualTo("Welcome to Link Shortener!");
                softly.assertThat(body).contains(expectedUrl);
            }
        } catch (MessagingException e) {
            throw new AssertionError("Failed while asserting email content", e);
        }
    }
}