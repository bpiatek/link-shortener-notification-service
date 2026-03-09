package pl.bpiatek.linkshortenernotificationservice.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.client.RestClient;
import pl.bpiatek.linkshortenernotificationservice.config.VaultHealthIndicator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceResiliencyTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private NotificationLogService notificationLogService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("Should handle Vault failure without affecting core notification processing flow")
    void shouldHandleVaultFailureGracefully() {
        // given
        var builderMock = mock(RestClient.Builder.class, RETURNS_SELF);
        var restClientMock = mock(RestClient.class, RETURNS_DEEP_STUBS);
        when(builderMock.build()).thenReturn(restClientMock);

        var vaultHealthIndicator = new VaultHealthIndicator(
                builderMock,
                "http://invalid-vault-url"
        );

        // when
        when(restClientMock.get()).thenThrow(new RuntimeException("Connection Refused"));


        // then
        var health = vaultHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        verifyNoInteractions(emailSender);
    }
}