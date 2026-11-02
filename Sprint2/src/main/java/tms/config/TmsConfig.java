package tms.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Clasa de configurare Spring a aplicației TMS: scanează serviciile domeniului
 * și declară infrastructura de care depind (ceas și validator JavaBean Validation).
 */
@Configuration
@ComponentScan(basePackages = {
        "tms.domain.support",
        "tms.service"
})
public class TmsConfig {

    @Bean
    public Clock clock() { return Clock.systemDefaultZone(); }

    @Bean
    public LocalValidatorFactoryBean validator() { return new LocalValidatorFactoryBean(); }
}
