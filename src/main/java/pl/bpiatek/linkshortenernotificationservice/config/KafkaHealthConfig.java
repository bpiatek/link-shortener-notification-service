package pl.bpiatek.linkshortenernotificationservice.config;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
class KafkaHealthConfig {

    @Bean
    KafkaAdmin kafkaAdmin(KafkaProperties kafkaProperties) {
        return new KafkaAdmin(kafkaProperties.buildAdminProperties(null));
    }
}