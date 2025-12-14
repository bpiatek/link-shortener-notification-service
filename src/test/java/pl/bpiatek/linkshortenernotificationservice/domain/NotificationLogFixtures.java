package pl.bpiatek.linkshortenernotificationservice.domain;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;

@Component
@ActiveProfiles("test")
public class NotificationLogFixtures {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert notificationInsert;

    public NotificationLogFixtures(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.notificationInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName("notification_logs")
                .usingGeneratedKeyColumns("id");
    }

    TestNotificationLog aNotificationLog(TestNotificationLog notificationLog) {
        var params = new MapSqlParameterSource()
                .addValue("event_id", notificationLog.getEventId())
                .addValue("recipient_email", notificationLog.getRecipientEmail())
                .addValue("notification_type", notificationLog.getNotificationType())
                .addValue("status", notificationLog.getStatus())
                .addValue("sent_at", Timestamp.from(notificationLog.getSentAt()))
                .addValue("error_message", notificationLog.getErrorMessage());

        notificationInsert.execute(params);

        return getByEventId(notificationLog.getEventId());
    }

    TestNotificationLog aNotificationLog() {
        return aNotificationLog(TestNotificationLog.builder().build());
    }

    TestNotificationLog getByEventId(String eventId) {
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

        try {
            var params = new MapSqlParameterSource().addValue("eventId", eventId);
            return namedParameterJdbcTemplate.queryForObject(sql, params, (rs, rowNum) ->
                    TestNotificationLog.builder()
                            .withId(rs.getLong("id"))
                            .withEventId(rs.getString("event_id"))
                            .withRecipientEmail(rs.getString("recipient_email"))
                            .withNotificationType(rs.getString("notification_type"))
                            .withStatus(rs.getString("status"))
                            .withSentAt(rs.getTimestamp("sent_at").toInstant())
                            .withErrorMessage(rs.getString("error_message"))
                            .build()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
