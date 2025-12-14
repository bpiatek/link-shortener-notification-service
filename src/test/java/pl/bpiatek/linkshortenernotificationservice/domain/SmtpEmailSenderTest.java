package pl.bpiatek.linkshortenernotificationservice.domain;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.assertj.core.api.Assertions.assertThat;

class SmtpEmailSenderTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("testuser", "testpass"))
            .withPerMethodLifecycle(true);

    @AfterEach
    void cleanup() {
        greenMail.reset();
    }

    @Test
    void shouldSendEmail() throws MessagingException {
        // given
        var to = "recipient@example.com";
        var subject = "Integration Test Subject";
        var body = "<p>This is a test.</p>";
        var from = "noreply@example.com";

        var emailSender = setUpEmailSender(from);

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

    private SmtpEmailSender setUpEmailSender(String from) {
        var javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("localhost");
        javaMailSender.setPort(greenMail.getSmtp().getPort());
        javaMailSender.setUsername("testuser");
        javaMailSender.setPassword("testpass");
        javaMailSender.setProtocol("smtp");

        var props = javaMailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return new SmtpEmailSender(javaMailSender, from);
    }
}