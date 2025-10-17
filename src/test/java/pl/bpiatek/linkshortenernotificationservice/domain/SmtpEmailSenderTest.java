package pl.bpiatek.linkshortenernotificationservice.domain;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
            org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@ActiveProfiles("test")
class SmtpEmailSenderTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("testuser", "testpass"))
            .withPerMethodLifecycle(true);

    @DynamicPropertySource
    static void configureMailProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", ServerSetupTest.SMTP::getPort);
        registry.add("spring.mail.username", () -> "testuser");
        registry.add("spring.mail.password", () -> "testpass");
    }

    @Autowired
    private EmailSender emailSender;

    @Test
    void shouldSendEmail() throws MessagingException {
        // given
        var to = "recipient@example.com";
        var subject = "Integration Test Subject";
        var body = "<p>This is a test.</p>";

        // when
        emailSender.send(to, subject, body);

        // then
        var receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages).hasSize(1);

        var receivedMessage = receivedMessages[0];
        assertThat(receivedMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString()).isEqualTo(to);
        assertThat(receivedMessage.getSubject()).isEqualTo(subject);
        var receivedBody = GreenMailUtil.getBody(receivedMessage);
        assertThat(receivedBody).contains(body);
    }
}