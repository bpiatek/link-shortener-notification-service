package pl.bpiatek.linkshortenernotificationservice.domain;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.MessagingException;
import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import pl.bpiatek.contracts.user.UserLifecycleEventProto;
import pl.bpiatek.contracts.user.UserLifecycleEventProto.UserLifecycleEvent;
import pl.bpiatek.linkshortenernotificationservice.IntegrationTest;

import java.util.UUID;

import static com.icegreen.greenmail.util.ServerSetup.PROTOCOL_SMTP;
import static jakarta.mail.Message.RecipientType.TO;
import static org.assertj.core.api.Assertions.assertThat;

class UserLifecycleKafkaConsumerTest extends IntegrationTest {

    @Autowired
    private KafkaTemplate<String, UserLifecycleEvent> kafkaTemplate;

    @Autowired
    private NotificationLogFixtures fixtures;

    @Value("${topic.user.lifecycle}")
    private String topicName;

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(
            new ServerSetup(DEFAULT_MAIL_PORT, "localhost", PROTOCOL_SMTP))
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("testuser", "testpass"));

    @AfterEach
    void cleanup() {
        greenMail.reset();
    }

    @Test
    void shouldConsumeUserRegisteredEventAndSendEmail() {
        // given
        var eventId = UUID.randomUUID().toString();
        var userEmail = "test-consumer@example.com";
        var verificationUrl = "https://shortener.pl/verify?token=123";
        var subject = "Welcome to Link Shortener!";
        var event = createUserRegisteredEvent(eventId, userEmail, verificationUrl);

        // when
        kafkaTemplate.send(topicName, event);

        // then
        assertEmailWasSent(userEmail, verificationUrl, subject);
        assertThat(fixtures.getByEventId(eventId)).isNotNull();
    }

    @Test
    void shouldConsumePasswordResetRequestedEventAndSendEmail() {
        // given
        var eventId = UUID.randomUUID().toString();
        var userEmail = "test-consumer@example.com";
        var resetUrl = "https://shortener.pl/reset-password?token=123";
        var subject = "Reset Your Password - Link Shortener";
        var event = createPasswordResetRequestedEvent(eventId, userEmail, resetUrl);

        // when
        kafkaTemplate.send(topicName, event);

        // then
        assertEmailWasSent(userEmail, resetUrl, subject);
        assertThat(fixtures.getByEventId(eventId)).isNotNull();
    }

    private UserLifecycleEvent createUserRegisteredEvent(String eventId, String email, String url) {
        var payload = UserLifecycleEventProto.UserRegistered.newBuilder()
                .setUserId("user-1")
                .setEmail(email)
                .setVerificationUrl(url)
                .build();

        return UserLifecycleEvent.newBuilder()
                .setEventId(eventId)
                .setUserRegistered(payload)
                .build();
    }

    private UserLifecycleEvent createPasswordResetRequestedEvent(String eventId, String email, String url) {
        var payload = UserLifecycleEventProto.PasswordResetRequested.newBuilder()
                .setEmail(email)
                .setResetUrl(url)
                .build();

        return UserLifecycleEvent.newBuilder()
                .setEventId(eventId)
                .setPasswordResetRequested(payload)
                .build();
    }

    private void assertEmailWasSent(String expectedTo, String expectedUrl, String subject) {
        try {
            assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();

            var receivedMessages = greenMail.getReceivedMessages();
            assertThat(receivedMessages).hasSize(1);

            var receivedMessage = receivedMessages[0];
            var body = GreenMailUtil.getBody(receivedMessage);

            try (var softly = new AutoCloseableSoftAssertions()) {
                softly.assertThat(receivedMessage.getRecipients(TO)[0].toString()).isEqualTo(expectedTo);
                softly.assertThat(receivedMessage.getSubject()).isEqualTo(subject);
                softly.assertThat(body).contains(expectedUrl);
            }
        } catch (MessagingException e) {
            throw new AssertionError("Failed while asserting email content", e);
        }
    }
}