package tms.persistence.listener;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * Contextul auditului pentru firul curent: utilizatorul care efectuează operația și ceasul folosit.
 * Listenerii JPA sunt instanțiați de provider (nu de Spring), deci primesc aceste informații printr-un context static.
 */
public final class AuditContext {

    public static final String UTILIZATOR_SISTEM = "system";

    private static final ThreadLocal<String> UTILIZATOR = new ThreadLocal<>();
    private static volatile Clock clock = Clock.systemDefaultZone();

    private AuditContext() { }

    public static void seteazaUtilizator(String utilizator) { UTILIZATOR.set(utilizator); }

    public static void curata() { UTILIZATOR.remove(); }

    /** Utilizatorul curent sau {@code null} dacă nu a fost setat explicit. */
    public static String utilizatorExplicit() { return UTILIZATOR.get(); }

    /** Utilizatorul curent; dacă nu există, „system”. */
    public static String utilizatorCurent() {
        String u = UTILIZATOR.get();
        return u == null ? UTILIZATOR_SISTEM : u;
    }

    /** Execută acțiunea având utilizatorul dat în context (de exemplu un {@code repository.save(...)}) și apoi curăță contextul. */
    public static <T> T ca(String utilizator, Supplier<T> actiune) {
        String anterior = UTILIZATOR.get();
        UTILIZATOR.set(utilizator);
        try {
            return actiune.get();
        } finally {
            if (anterior == null) UTILIZATOR.remove(); else UTILIZATOR.set(anterior);
        }
    }

    public static void seteazaCeas(Clock ceas) { clock = ceas; }

    public static LocalDateTime acum() { return LocalDateTime.now(clock); }
}
