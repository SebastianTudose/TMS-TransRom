package tms.support;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import tms.domain.entity.Camion;
import tms.domain.entity.Client;
import tms.domain.entity.ComandaTransport;
import tms.domain.entity.Dispecer;
import tms.domain.entity.Sofer;
import tms.domain.enums.Moneda;
import tms.domain.support.ComandaTransportFactory;
import tms.domain.vo.AdresaEmail;
import tms.domain.vo.SumaMonetara;
import tms.persistence.repository.CamionRepository;
import tms.persistence.repository.ClientRepository;
import tms.persistence.repository.SoferRepository;
import tms.service.workflow.ComandaWorkflowService;

/** Date de test persistente: obiecte construite ca în {@link TestData}, dar salvate prin repository-uri (cu id generat de baza de date). */
public final class PersistentData {

    private PersistentData() { }

    public static Camion camion(CamionRepository repo, String nr, String capacitateKg) {
        Camion c = TestData.camion(0, nr, capacitateKg);
        c.setId(null);
        return repo.save(c);
    }

    public static Sofer sofer(SoferRepository repo, String sufix, LocalDate expirarePermis) {
        Sofer s = TestData.sofer(0, expirarePermis);
        s.setId(null);
        s.setEmail(new AdresaEmail("sofer" + sufix + "@tms.ro"));
        return repo.save(s);
    }

    public static Client client(ClientRepository repo, String codFiscal, String discount, Moneda moneda) {
        Client c = TestData.client(0, discount, moneda);
        c.setId(null);
        c.setCodFiscal(codFiscal);
        return repo.save(c);
    }

    /** Comandă nouă (nesalvată) cu o singură linie: cantitate x greutateUnitara kg. */
    public static ComandaTransport comandaNoua(ComandaTransportFactory f, Long clientId, String tarif, Moneda moneda,
                                               int cantitate, String greutateUnitara) {
        return f.creeaza(clientId, TestData.IASI, TestData.MUNCHEN, SumaMonetara.of(tarif, moneda), null,
                List.of(f.creeazaLinie("Paleți", cantitate, new BigDecimal(greutateUnitara), new BigDecimal("0.8"))));
    }

    /** Comandă creată prin workflow (deci validată, salvată și auditată). */
    public static ComandaTransport comanda(ComandaTransportFactory f, ComandaWorkflowService workflow, Dispecer dispecer,
                                           Long clientId, String tarif, Moneda moneda, int cantitate, String greutateUnitara) {
        return workflow.creeazaComanda(dispecer, comandaNoua(f, clientId, tarif, moneda, cantitate, greutateUnitara));
    }
}
