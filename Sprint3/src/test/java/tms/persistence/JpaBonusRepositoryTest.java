package tms.persistence;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tms.domain.entity.Camion;
import tms.domain.entity.Client;
import tms.domain.enums.Moneda;
import tms.domain.vo.NrInmatriculare;
import tms.persistence.jpa.ICamionRepository;
import tms.persistence.jpa.IClientRepository;
import tms.support.TestData;

/** Bonus: repository-urile scrise manual cu EntityManager (IRepository + implementări JPA). */
class JpaBonusRepositoryTest extends PersistenceTestBase {

    @Autowired IClientRepository clienti;
    @Autowired ICamionRepository camioane;

    private Client clientNou(String cf) {
        Client c = TestData.client(0, "0", Moneda.EUR);
        c.setId(null);
        c.setCodFiscal(cf);
        return c;
    }

    @Test void saveFaceInsertSiUpdate() {
        Client c = clienti.save(clientNou("RO1"));
        assertNotNull(c.getId());
        c.setTelefon("0232111222");
        Client salvat = clienti.save(c);
        sincronizeaza();
        assertEquals("0232111222", clienti.findById(salvat.getId()).orElseThrow().getTelefon());
        assertEquals(1, clienti.count());
    }

    @Test void finderiiSiStergerea() {
        Client c = clienti.save(clientNou("RO1"));
        assertTrue(clienti.findByCodFiscal("RO1").isPresent());
        assertTrue(clienti.existsById(c.getId()));
        clienti.deleteById(c.getId());
        sincronizeaza();
        assertFalse(clienti.existsById(c.getId()));
        assertTrue(clienti.findAll().isEmpty());
    }

    @Test void camioaneleDupaNumar() {
        Camion c = TestData.camion(0, "IS99ZZZ", "10000");
        c.setId(null);
        camioane.save(c);
        sincronizeaza();
        assertTrue(camioane.findByNrInmatriculare(new NrInmatriculare("IS99ZZZ")).isPresent());
        assertEquals(1, camioane.findAll().size());
    }
}
