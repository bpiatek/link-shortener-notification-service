package pl.bpiatek.linkshortenernotificationservice.domain;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;
import java.util.Optional;

class JdbcNotificationLogRepository implements NotificationLogRepository {

    private static final NotificationLogRowMapper ROW_MAPPER = new NotificationLogRowMapper();
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    JdbcNotificationLogRepository(JdbcTemplate jdbcTemplate) {
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
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

    @Override
    public void update(NotificationLog log) {
        var sql = """
            UPDATE notification_logs 
            SET status = :status, 
                error_message = :errorMessage 
            WHERE event_id = :eventId
            """;

        var params = new MapSqlParameterSource()
                .addValue("eventId", log.eventId())
                .addValue("status", log.status())
                .addValue("errorMessage", log.errorMessage());

        namedJdbcTemplate.update(sql, params);
    }

    @Override
    public Optional<NotificationLog> findByEventId(String eventId) {
        var sql = """
            SELECT
            n.id,
            n.event_id,
            n.recipient_email,
            n.notification_type,
            n.status,
            n.sent_at,
            n.error_message
            FROM notification_logs n WHERE event_id = :eventId""";

        var params = new MapSqlParameterSource().addValue("eventId", eventId);

        var result = namedJdbcTemplate.query(sql, params, ROW_MAPPER);

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(result.getFirst());
    }
}
