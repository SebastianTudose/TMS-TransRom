package tms.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tms.persistence.config.DataSourceConfig;
import tms.persistence.config.JpaConfig;
import tms.persistence.listener.AuditContext;

/**
 * Clasa de configurare Spring a aplicației TMS: importă configurarea persistenței (sursă de date H2 și JPA),
 * scanează serviciile domeniului și declară infrastructura de care depind (ceas și validator JavaBean Validation).
 */
@Configuration
@Import({DataSourceConfig.class, JpaConfig.class})
@ComponentScan(basePackages = {
        "tms.domain.support",
        "tms.service",
        "tms.persistence.jpa"
})
public class TmsConfig {

    @Bean
    public Clock clock() {
        Clock ceas = Clock.systemDefaultZone();
        AuditContext.seteazaCeas(ceas);     // listenerii JPA (neinstanțiați de Spring) folosesc același ceas
        return ceas;
    }

    @Bean
    public LocalValidatorFactoryBean validator() { return new LocalValidatorFactoryBean(); }
}
