package pl.bpiatek.linkshortenernotificationservice.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.bpiatek.linkshortenernotificationservice.IntegrationTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class JdbcNotificationLogRepositoryTest extends IntegrationTest {

    @Autowired
    JdbcNotificationLogRepository notificationLogRepository;

    @Autowired
    NotificationLogFixtures notificationLogFixtures;

    @Test
    void shouldSaveNotificationLog() {
        // given
        var now = Instant.parse("2025-08-22T10:00:00Z");
        var eventId = "a9a457f0-8d71-3a52-ae96-2587d2f9c75b";
        var log = new NotificationLog(
                null,
                eventId,
                "test@example.com",
                "WELCOME_EMAIL",
                "SENT",
                now,
                null
        );

        // when
        notificationLogRepository.save(log);

        // then
        var savedLog = notificationLogFixtures.getByEventId(eventId);
        assertThat(savedLog).isNotNull();
        assertSoftly(s -> {
            s.assertThat(savedLog.getEventId()).isEqualTo(eventId);
            s.assertThat(savedLog.getRecipientEmail()).isEqualTo(log.recipientEmail());
            s.assertThat(savedLog.getNotificationType()).isEqualTo(log.notificationType());
            s.assertThat(savedLog.getStatus()).isEqualTo(log.status());
            s.assertThat(savedLog.getSentAt()).isEqualTo(log.sentAt());
            s.assertThat(savedLog.getErrorMessage()).isNull();
        });
    }

    @Test
    void shouldUpdateNotificationLog() {
        // given
        var log = notificationLogFixtures.aNotificationLog();
        var updatedLog = new NotificationLog(
                null, log.getEventId(),
                null,
                null,
                "ERROR",
                null,
                "not send");

        // when
        notificationLogRepository.update(updatedLog);

        // then
        var savedLog = notificationLogFixtures.getByEventId(log.getEventId());
        assertThat(savedLog).isNotNull();
        assertSoftly(s -> {
            s.assertThat(savedLog.getEventId()).isEqualTo(log.getEventId());
            s.assertThat(savedLog.getRecipientEmail()).isEqualTo(log.getRecipientEmail());
            s.assertThat(savedLog.getNotificationType()).isEqualTo(log.getNotificationType());
            s.assertThat(savedLog.getStatus()).isEqualTo(updatedLog.status());
            s.assertThat(savedLog.getSentAt()).isEqualTo(log.getSentAt());
            s.assertThat(savedLog.getErrorMessage()).isEqualTo(updatedLog.errorMessage());
        });
    }

    @Test
    void shouldFindNotificationLogById() {
        // given
        var log = notificationLogFixtures.aNotificationLog();

        // when
        var foundLog = notificationLogRepository.findByEventId(log.getEventId());

        // then
        assertThat(foundLog).isPresent();
        var actual = foundLog.get();
        assertSoftly(s -> {
            s.assertThat(actual.id()).isEqualTo(log.getId());
            s.assertThat(actual.eventId()).isEqualTo(log.getEventId());
            s.assertThat(actual.recipientEmail()).isEqualTo(log.getRecipientEmail());
            s.assertThat(actual.notificationType()).isEqualTo(log.getNotificationType());
            s.assertThat(actual.status()).isEqualTo(log.getStatus());
            s.assertThat(actual.sentAt()).isEqualTo(log.getSentAt());
            s.assertThat(actual.errorMessage()).isEqualTo(log.getErrorMessage());
        });
    }
}