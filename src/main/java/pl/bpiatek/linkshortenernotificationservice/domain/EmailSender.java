package pl.bpiatek.linkshortenernotificationservice.domain;

public interface EmailSender {
    void send(String to, String subject, String body);
}
