package pl.bpiatek.linkshortenernotificationservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import pl.bpiatek.linkshortenernotificationservice.domain.NotificationLogFixtures;

@TestConfiguration
public class TestDatabaseConfiguration {

    @Bean
    public NotificationLogFixtures notificationLogFixtures(NamedParameterJdbcTemplate jdbcTemplate) {
        return new NotificationLogFixtures(jdbcTemplate);
    }
}
