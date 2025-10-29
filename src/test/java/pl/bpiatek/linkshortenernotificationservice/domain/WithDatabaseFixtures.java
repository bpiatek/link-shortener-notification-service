package pl.bpiatek.linkshortenernotificationservice.domain;

import org.springframework.context.annotation.Import;
import pl.bpiatek.linkshortenernotificationservice.config.TestDatabaseConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(TestDatabaseConfiguration.class)
public @interface WithDatabaseFixtures {
}
