package pl.bpiatek.linkshortenernotificationservice.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@JdbcTest
@Import({JdbcNotificationLogRepository.class, NotificationLogFixtures.class})
@ActiveProfiles("test")
class JdbcNotificationLogRepositoryTest implements WithPostgres{

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    JdbcNotificationLogRepository notificationLogRepository;

    @Autowired
    NotificationLogFixtures notificationLogFixtures;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM notification_logs");
    }

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
    void shouldConfirmThatNotificationWithGivenEventIdAlreadyExist() {
        // given
        var notificationLog = notificationLogFixtures.aNotificationLog();

        // when
        var exist = notificationLogRepository.existsByEventId(notificationLog.getEventId());

        // then
        assertThat(exist).isTrue();
    }

    @Test
    void shouldConfirmThatNotificationWithGivenEventIdDoesNotExist() {
        // given
        var eventId = "non-existent-event-id";

        // when
        var exist = notificationLogRepository.existsByEventId(eventId);

        // then
        assertThat(exist).isFalse();
    }
}