package tms.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import tms.config.TmsConfig;
import tms.persistence.listener.AuditContext;

/** Bază comună: context Spring cu H2 în memorie, fiecare test într-o tranzacție anulată la final. */
@SpringJUnitConfig(TmsConfig.class)
@Transactional
public abstract class PersistenceTestBase {

    @PersistenceContext
    protected EntityManager em;

    /** Trimite modificările în baza de date și golește contextul, ca următoarea citire să vină din bază. */
    protected void sincronizeaza() {
        em.flush();
        em.clear();
    }

    protected <T> T ca(String utilizator, java.util.function.Supplier<T> actiune) {
        return AuditContext.ca(utilizator, actiune);
    }

    /** Cauza rădăcină a unei excepții (Hibernate împachetează erorile din listeneri). */
    protected static Throwable radacina(Throwable t) {
        while (t.getCause() != null && t.getCause() != t) t = t.getCause();
        return t;
    }
}
