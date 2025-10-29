package pl.bpiatek.linkshortenernotificationservice.domain;

import java.time.Instant;

record NotificationLog(
        Long id,
        String eventId,
        String recipientEmail,
        String notificationType,
        String status,
        Instant sentAt,
        String errorMessage) {
}
