package pl.bpiatek.linkshortenernotificationservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Component("vault")
class VaultHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(VaultHealthIndicator.class);
    private final RestClient restClient;
    private final String vaultHealthUrl;

    VaultHealthIndicator(
            RestClient.Builder restClientBuilder,
            @Value("${vault.address:http://vault.vault.svc.cluster.local:8200}") String vaultAddress) {

        // ARCHITECTURAL FIX 1: Force HTTP/1.1
        // Prevents the JDK SelectorManager thread from entering an infinite CPU spin loop
        // when the HTTP/2 stream is abruptly closed by the Go server.
        var httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                // ARCHITECTURAL FIX 2: Override the default error handler
                // We tell Spring NOT to throw exceptions for 4xx/5xx errors, allowing us
                // to gracefully handle Vault's 503 SEALED status in the switch block.
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> { /* No-op */ })
                .build();

        this.vaultHealthUrl = vaultAddress + "/v1/sys/health";
    }

    @Override
    public Health health() {
        try {
            // Because of the custom status handler, this will now safely return the 503
            // response body-less entity instead of throwing an exception.
            var response = restClient.get()
                    .uri(vaultHealthUrl)
                    .retrieve()
                    .toBodilessEntity();

            var status = response.getStatusCode().value();

            return switch (status) {
                case 200 -> Health.up()
                        .withDetail("state", "ACTIVE")
                        .build();
                case 503 -> Health.down()
                        .withDetail("state", "SEALED")
                        .withDetail("action", "Manual unseal required on vault-0")
                        .build();
                case 501 -> Health.down()
                        .withDetail("state", "UNINITIALIZED")
                        .build();
                default -> Health.down()
                        .withDetail("state", "UNKNOWN")
                        .withDetail("http_status", status)
                        .build();
            };

        } catch (Exception e) {
            // This catch block will now ONLY execute for genuine network failures
            // (e.g., Vault pod is dead / Connection Refused)
            log.error("Failed to connect to Vault at {}", vaultHealthUrl);
            return Health.down(e)
                    .withDetail("error", "Vault connection refused")
                    .build();
        }
    }
}