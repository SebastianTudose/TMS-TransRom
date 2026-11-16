package tms.persistence.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 * Sursa de date implicită: bază H2 în memorie (dezvoltare și teste). Pentru PostgreSQL / MySQL se definește
 * un alt bean {@link DataSource} și se elimină importul acestei clase din {@code TmsConfig}.
 */
@Configuration
public class DataSourceConfig {

    @Bean(destroyMethod = "shutdown")
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }
}
