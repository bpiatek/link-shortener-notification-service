package pl.bpiatek.linkshortenernotificationservice.domain;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

class NotificationLogRowMapper implements RowMapper<NotificationLog> {

    @Override
    public NotificationLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new NotificationLog(
                rs.getLong("id"),
                rs.getString("event_id"),
                rs.getString("recipient_email"),
                rs.getString("notification_type"),
                rs.getString("status"),
                rs.getTimestamp("sent_at").toInstant(),
                rs.getString("error_message")
        );
    }
}
