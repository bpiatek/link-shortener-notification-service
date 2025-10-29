package pl.bpiatek.linkshortenernotificationservice.domain;

import java.time.Instant;

class TestNotificationLog {

    private final Long id;
    private final String eventId;
    private final String recipientEmail;
    private final String notificationType;
    private final String status;
    private final Instant sentAt;
    private final String errorMessage;

    public  TestNotificationLog(TestNotificationLogBuilder builder) {
        this.id = builder.id;
        this.eventId = builder.eventId;
        this.recipientEmail = builder.recipientEmail;
        this.notificationType = builder.notificationType;
        this.status = builder.status;
        this.sentAt = builder.sentAt;
        this.errorMessage = builder.errorMessage;
    }

    static TestNotificationLogBuilder builder() {
        return new TestNotificationLogBuilder();
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getStatus() {
        return status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    static class TestNotificationLogBuilder {
        private Long id;
        private String eventId = "a9a457f0-8d71-3a52-ae96-2587d2f9c75b";
        private String recipientEmail = "test@example.com";
        private String notificationType = "WELCOME_EMAIL";
        private String status = "SENT";
        private Instant sentAt = Instant.parse("2025-01-01T10:00:00Z");
        private String errorMessage;

        public TestNotificationLogBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public TestNotificationLogBuilder withEventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public TestNotificationLogBuilder withRecipientEmail(String recipientEmail) {
            this.recipientEmail = recipientEmail;
            return this;
        }

        public TestNotificationLogBuilder withNotificationType(String notificationType) {
            this.notificationType = notificationType;
            return this;
        }

        public TestNotificationLogBuilder withStatus(String status) {
            this.status = status;
            return this;
        }

        public TestNotificationLogBuilder withSentAt(Instant sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public TestNotificationLogBuilder withErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public TestNotificationLog build() {
            return new TestNotificationLog(this);
        }
    }
}
