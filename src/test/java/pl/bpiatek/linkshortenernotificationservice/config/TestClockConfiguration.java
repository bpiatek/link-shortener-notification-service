package pl.bpiatek.linkshortenernotificationservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(ClockConfig.class)
public class TestClockConfiguration {
}
