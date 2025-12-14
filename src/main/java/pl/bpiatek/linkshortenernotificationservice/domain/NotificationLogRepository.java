package pl.bpiatek.linkshortenernotificationservice.domain;

import java.util.Optional;

interface NotificationLogRepository {

    void save(NotificationLog log);

    void update(NotificationLog log);

    Optional<NotificationLog> findByEventId(String eventId);
}
