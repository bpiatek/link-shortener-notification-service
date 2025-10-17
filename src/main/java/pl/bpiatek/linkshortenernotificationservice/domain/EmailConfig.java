package pl.bpiatek.linkshortenernotificationservice.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
class EmailConfig {

    @Bean
    EmailSender emailSender(@Value("${app.mail.from}") String fromEmail, JavaMailSender mailSender) {
        return new SmtpEmailSender(mailSender, fromEmail);
    }

}
