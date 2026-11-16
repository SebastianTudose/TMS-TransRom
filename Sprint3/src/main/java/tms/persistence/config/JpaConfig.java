package tms.persistence.config;

import jakarta.persistence.EntityManagerFactory;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configurarea persistenței: EntityManagerFactory (Hibernate), manager de tranzacții și repository-urile Spring Data.
 * Entitățile sunt căutate în pachetele {@code tms.domain} (entități, obiecte valoare) și {@code tms.persistence}
 * (convertoare, entități tehnice).
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "tms.persistence.repository")
public class JpaConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("tms.domain", "tms.persistence");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Properties p = new Properties();
        p.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        p.setProperty("hibernate.format_sql", "true");
        // regulile de business sunt verificate de ValidationService (cu coduri RB-xx), nu la persistare
        p.setProperty("jakarta.persistence.validation.mode", "none");
        emf.setJpaProperties(p);
        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
