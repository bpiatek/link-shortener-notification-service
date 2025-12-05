package pl.bpiatek.linkshortenernotificationservice.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import pl.bpiatek.contracts.user.UserLifecycleEventProto;

import java.util.Map;

class UserLifecycleKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserLifecycleKafkaConsumer.class);
    private final NotificationService notificationService;

    UserLifecycleKafkaConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "${topic.user.lifecycle}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "userLifecycleEventContainerFactory"
    )
    void consume(UserLifecycleEventProto.UserLifecycleEvent event,
                 @Headers Map<String, Object> headers) {
        log.info("Received headers: {}", headers);
        log.info("Received user lifecycle event: {}", event);

        switch (event.getEventPayloadCase()) {
            case USER_REGISTERED:
                notificationService.processUserRegistration(event.getEventId(), event.getUserRegistered());
                break;
                //TODO more in the future
            default:
                log.warn("Received unhandled event type: {}", event.getEventPayloadCase());
        }
    }
}
