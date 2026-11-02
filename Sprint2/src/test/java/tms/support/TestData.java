package tms.support;

import java.math.BigDecimal;
import java.time.LocalDate;
import tms.domain.entity.*;
import tms.domain.enums.Moneda;
import tms.domain.support.ComandaTransportFactory;
import tms.domain.vo.*;

/** Date de test reutilizabile (fixture). */
public final class TestData {
    private TestData() { }

    public static final Locatie IASI = new Locatie("RO", "Iași", "Str. Palat 1");
    public static final Locatie MUNCHEN = new Locatie("DE", "München", "Hauptstraße 5");

    private static <U extends Utilizator> U cont(U u, long id, String email) {
        u.setId(id); u.setNume("Popescu"); u.setPrenume("Ion");
        u.setEmail(new AdresaEmail(email)); u.setHashParola("hash");
        return u;
    }
    public static Dispecer dispecer() { return cont(new Dispecer(), 1, "dispecer@tms.ro"); }
    public static Contabil contabil() { return cont(new Contabil(), 2, "contabil@tms.ro"); }
    public static Administrator administrator() { return cont(new Administrator(), 3, "admin@tms.ro"); }
    public static Sofer sofer(long id, LocalDate expirarePermis) {
        Sofer s = cont(new Sofer(), id, "sofer" + id + "@tms.ro");
        s.setSeriePermis("B1234567"); s.setDataExpirarePermis(expirarePermis);
        return s;
    }
    public static Camion camion(long id, String nr, String capacitateKg) {
        Camion c = new Camion();
        c.setId(id); c.setNrInmatriculare(new NrInmatriculare(nr)); c.setMarca("Volvo"); c.setModel("FH16");
        c.setAnFabricatie(2020); c.setCapacitateKg(new BigDecimal(capacitateKg));
        return c;
    }
    public static Client client(long id, String discount, Moneda moneda) {
        Client c = new Client();
        c.setId(id); c.setDenumire("Alfa Logistic SRL"); c.setCodFiscal("RO12345678");
        c.setContBancar(new Iban("RO49AAAA1B31007593840000"));
        c.setDiscountAcordat(new BigDecimal(discount)); c.setMonedaFacturare(moneda);
        return c;
    }
    /** Comandă cu o singură linie: cantitate x greutateUnitara kg. */
    public static ComandaTransport comanda(ComandaTransportFactory f, long id, long clientId,
                                           String tarif, Moneda moneda, int cantitate, String greutateUnitara) {
        ComandaTransport c = f.creeaza(clientId, IASI, MUNCHEN, SumaMonetara.of(tarif, moneda), null,
                java.util.List.of(f.creeazaLinie("Paleți", cantitate, new BigDecimal(greutateUnitara), new BigDecimal("0.8"))));
        c.setId(id);
        return c;
    }
}
