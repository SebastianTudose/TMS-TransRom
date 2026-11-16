package tms.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tms.domain.entity.*;
import tms.domain.enums.Moneda;
import tms.domain.exception.BusinessRuleException;
import tms.domain.vo.AdresaEmail;
import tms.persistence.entity.SecventaCod;
import tms.persistence.repository.*;
import tms.service.audit.AuditService;
import tms.support.PersistentData;
import tms.support.TestData;

/** Utilizatori (SINGLE_TABLE), jurnalul de audit persistent și secvențele de coduri. */
class UtilizatorAuditSecventaTest extends PersistenceTestBase {

    @Autowired UtilizatorRepository utilizatori;
    @Autowired SoferRepository soferi;
    @Autowired AuditEntryRepository auditRepo;
    @Autowired SecventaCodRepository secvente;
    @Autowired AuditService audit;

    @Test void ierarhiaUtilizatorilorSeSalveazaInTabelaUnica() {
        Sofer s = PersistentData.sofer(soferi, "1", LocalDate.now().plusYears(1));
        Dispecer d = TestData.dispecer();
        d.setId(null);
        utilizatori.save(d);
        sincronizeaza();
        assertEquals(2, utilizatori.findAll().size());
        assertEquals(1, soferi.findAll().size());
        assertTrue(utilizatori.findById(s.getId()).orElseThrow() instanceof Sofer);
        assertTrue(utilizatori.findByEmail(new AdresaEmail("dispecer@tms.ro")).orElseThrow() instanceof Dispecer);
        assertTrue(utilizatori.existsByEmail(new AdresaEmail("sofer1@tms.ro")));
        assertEquals(2, utilizatori.findByActivTrue().size());
    }

    @Test void auditulSePersistaSiSeCitesteInOrine() {
        Client c = PersistentData.client(clienti(), "RO1", "0", Moneda.EUR);
        audit.inregistreazaCreare(c, "dispecer@tms.ro");
        audit.inregistreazaModificare(c, "dispecer@tms.ro", "al doilea");
        sincronizeaza();
        var istoric = audit.istoric(c);
        assertEquals(2, istoric.size());
        assertEquals("al doilea", istoric.get(1).detalii());
        assertEquals(2, auditRepo.findByTipEntitateAndIdEntitateOrderByIdAsc("Client", c.getId()).size());
    }

    @Test void secventaIncrementeazaSiSePastreazaInBaza() {
        SecventaCod s = new SecventaCod("X-2026");
        s.urmatoareaValoare();
        s.urmatoareaValoare();
        secvente.saveAndFlush(s);
        em.clear();
        assertEquals(2, secvente.findByCheie("X-2026").orElseThrow().getUltimaValoare());
    }

    @Autowired ClientRepository clientRepo;
    private ClientRepository clienti() { return clientRepo; }
}
