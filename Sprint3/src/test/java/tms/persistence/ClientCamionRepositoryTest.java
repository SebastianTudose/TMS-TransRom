package tms.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import tms.domain.entity.Camion;
import tms.domain.entity.Client;
import tms.domain.enums.Moneda;
import tms.domain.enums.StatusCamion;
import tms.domain.vo.NrInmatriculare;
import tms.persistence.repository.CamionRepository;
import tms.persistence.repository.ClientRepository;
import tms.support.PersistentData;

class ClientCamionRepositoryTest extends PersistenceTestBase {

    @Autowired ClientRepository clienti;
    @Autowired CamionRepository camioane;

    @Test void clientulSeSalveazaCuIdSiAudit() {
        Client c = ca("admin@tms.ro", () -> PersistentData.client(clienti, "RO111", "5", Moneda.EUR));
        assertNotNull(c.getId());
        assertEquals("admin@tms.ro", c.getCreatedByUser());
        assertNotNull(c.getDateCreated());
        assertEquals(Integer.valueOf(0), c.getVersion());
    }

    @Test void finderiiClientului() {
        PersistentData.client(clienti, "RO111", "5", Moneda.EUR);
        Client ron = PersistentData.client(clienti, "RO222", "0", Moneda.RON);
        ron.setSoldDebitor(new BigDecimal("100"));
        sincronizeaza();
        assertTrue(clienti.findByCodFiscal("RO111").isPresent());
        assertTrue(clienti.existsByCodFiscal("RO222"));
        assertFalse(clienti.existsByCodFiscal("XX"));
        assertEquals(1, clienti.findByMonedaFacturare(Moneda.RON).size());
        assertEquals(2, clienti.findByDenumireContainingIgnoreCase("client").size());
        assertEquals(List.of("RO222"), clienti.findCuSoldDebitor().stream().map(Client::getCodFiscal).toList());
    }

    @Test void codFiscalulEsteUnic() {
        PersistentData.client(clienti, "RO111", "0", Moneda.EUR);
        Client dublura = PersistentData.client(clienti, "RO111", "0", Moneda.EUR);
        assertThrows(DataIntegrityViolationException.class, () -> clienti.saveAndFlush(dublura));
    }

    @Test void convertorulPastreazaValueObjectDupaReincarcare() {
        Camion c = PersistentData.camion(camioane, "IS12ABC", "20000");
        Long id = c.getId();
        sincronizeaza();
        Camion citit = camioane.findById(id).orElseThrow();
        assertEquals(new NrInmatriculare("IS12ABC"), citit.getNrInmatriculare());
        assertTrue(camioane.findByNrInmatriculare(new NrInmatriculare("IS12ABC")).isPresent());
        assertTrue(camioane.existsByNrInmatriculare(new NrInmatriculare("IS12ABC")));
    }

    @Test void finderiiCamionului() {
        PersistentData.camion(camioane, "IS11AAA", "10000");
        Camion mare = PersistentData.camion(camioane, "IS22BBB", "24000");
        mare.setStatusTehnic(StatusCamion.IN_SERVICE);
        sincronizeaza();
        assertEquals(1, camioane.findByStatusTehnic(StatusCamion.IN_SERVICE).size());
        List<Camion> potrivite = camioane.findCuCapacitateMinima(StatusCamion.DISPONIBIL, new BigDecimal("5000"));
        assertEquals(1, potrivite.size());
        assertTrue(camioane.findCuCapacitateMinima(StatusCamion.DISPONIBIL, new BigDecimal("15000")).isEmpty());
    }

    @Test void nrInmatriculareEsteUnic() {
        PersistentData.camion(camioane, "IS11AAA", "10000");
        Camion dublura = PersistentData.camion(camioane, "IS11AAA", "12000");
        assertThrows(DataIntegrityViolationException.class, () -> camioane.saveAndFlush(dublura));
    }
}
