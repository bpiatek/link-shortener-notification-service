package pl.bpiatek.linkshortenernotificationservice.domain;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;

class JdbcNotificationLogRepository implements NotificationLogRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    JdbcNotificationLogRepository(JdbcTemplate jdbcTemplate) {
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public boolean existsByEventId(String eventId) {
        var sql = "SELECT COUNT(*) FROM notification_logs WHERE event_id = :eventId";
        var params = new MapSqlParameterSource().addValue("eventId", eventId);

        var count = namedJdbcTemplate.queryForObject(sql, params, Integer.class);

        return count != null && count > 0;
    }

    @Override
    public void save(NotificationLog log) {
        var sql = """
            INSERT INTO notification_logs (event_id, recipient_email, notification_type, status, sent_at, error_message)
            VALUES (:eventId, :recipientEmail, :notificationType, :status, :sentAt, :errorMessage)
            """;

        var params = new MapSqlParameterSource()
                .addValue("eventId", log.eventId())
                .addValue("recipientEmail", log.recipientEmail())
                .addValue("notificationType", log.notificationType())
                .addValue("status", log.status())
                .addValue("sentAt", Timestamp.from(log.sentAt()))
                .addValue("errorMessage", log.errorMessage());

        namedJdbcTemplate.update(sql, params);
    }
}
