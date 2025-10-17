package pl.bpiatek.linkshortenernotificationservice.api;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bpiatek.linkshortenernotificationservice.domain.EmailSender;

@RestController
@RequestMapping("/notifications")
@Profile("dev")
class TestEmailController {

    private final EmailSender emailSender;

    TestEmailController(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public record TestEmailRequest(String to, String subject, String body) {}

    @PostMapping("/test/send-email")
    public ResponseEntity<String> sendTestEmail(@RequestBody TestEmailRequest request) {
        var htmlBody = "<h1>Test Email</h1><p>" + request.body() + "</p>";
        emailSender.send(request.to(), request.subject(), htmlBody);
        return ResponseEntity.ok("Test email sent to " + request.to());
    }
}
