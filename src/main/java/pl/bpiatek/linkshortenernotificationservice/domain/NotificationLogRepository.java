package pl.bpiatek.linkshortenernotificationservice.domain;

interface NotificationLogRepository {

    boolean existsByEventId(String eventId);

    void save(NotificationLog log);
}
