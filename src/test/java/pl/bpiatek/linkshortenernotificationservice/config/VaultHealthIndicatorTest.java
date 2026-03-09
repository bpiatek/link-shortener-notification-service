package pl.bpiatek.linkshortenernotificationservice.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaultHealthIndicatorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient vaultRestClient;

    @InjectMocks
    private VaultHealthIndicator healthIndicator;

    @Test
    void shouldReturnDownWhenVaultIsSealed() {
        // given
        ResponseEntity<Void> response = ResponseEntity.status(503).build();
        var responseSpecMock = mock(RestClient.ResponseSpec.class);
        when(vaultRestClient.get()
                .uri(anyString())
                .retrieve())
                .thenReturn(responseSpecMock);
        when(responseSpecMock.toBodilessEntity()).thenReturn(response);

        // when
        var health = healthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(org.springframework.boot.actuate.health.Status.DOWN);
        assertThat(health.getDetails()).containsEntry("state", "SEALED");
    }
}