package pl.bpiatek.linkshortenernotificationservice.domain;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

class NotificationLogService {
    private final NotificationLogRepository logRepository;
    private final Clock clock;

    NotificationLogService(NotificationLogRepository logRepository, Clock clock) {
        this.logRepository = logRepository;
        this.clock = clock;
    }

    public boolean existsByEventId(String eventId) {
        return logRepository.findByEventId(eventId).isPresent();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(String eventId, String email, String type, String status, String error) {
        var logEntry = new NotificationLog(
                null,
                eventId,
                email,
                type,
                status,
                clock.instant(),
                error
        );
        logRepository.save(logEntry);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLogStatus(String eventId, String status, String errorMessage) {
        var updateEntry = new NotificationLog(
                null,
                eventId,
                null,
                null,
                status,
                null,
                errorMessage
        );
        logRepository.update(updateEntry);
    }
}
