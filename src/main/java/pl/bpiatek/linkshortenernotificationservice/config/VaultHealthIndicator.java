package pl.bpiatek.linkshortenernotificationservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component("vault")
class VaultHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(VaultHealthIndicator.class);

    private final RestClient restClient;
    private final String vaultHealthUrl;

    VaultHealthIndicator(
            RestClient.Builder restClientBuilder,
            @Value("${vault.address:http://vault.vault.svc.cluster.local:8200}") String vaultAddress) {
        this.restClient = restClientBuilder.build();
        this.vaultHealthUrl = vaultAddress + "/v1/sys/health";
    }

    @Override
    public Health health() {
        log.debug("Executing Vault health check against: {}", vaultHealthUrl);

        try {
            var response = restClient.get()
                    .uri(vaultHealthUrl)
                    .retrieve()
                    .toBodilessEntity();

            if (response.getStatusCode().is2xxSuccessful()) {
                return Health.up()
                        .withDetail("vault", "reachable")
                        .withDetail("status", response.getStatusCode().value())
                        .build();
            }

            log.warn("Vault health check failed with status: {}", response.getStatusCode());
            return Health.down()
                    .withDetail("error", "Vault responded with non-200 status")
                    .withDetail("status", response.getStatusCode().value())
                    .build();

        } catch (RestClientException e) {
            log.error("Critical failure: Cannot reach HashiCorp Vault", e);
            // Passing the exception automatically prints the stack trace in the /actuator/health JSON
            return Health.down(e)
                    .withDetail("error", "Connection refused or timed out")
                    .build();
        }
    }
}