package tms.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import tms.config.TmsConfig;
import tms.domain.entity.*;
import tms.domain.enums.*;
import tms.domain.exception.BusinessRuleException;
import tms.domain.support.*;
import tms.domain.vo.IntervalTimp;
import tms.persistence.repository.CamionRepository;
import tms.persistence.repository.ClientRepository;
import tms.persistence.repository.SoferRepository;
import tms.support.PersistentData;
import tms.support.TestData;

/** Testează serviciile de suport ale entităților: secvențe persistente de coduri și fabricile de agregate. */
@SpringJUnitConfig(TmsConfig.class)
@Transactional
class SupportServicesTest {

    @Autowired SecventaCoduriService coduri;
    @Autowired RutaFactory rutaFactory;
    @Autowired ComandaTransportFactory comandaFactory;
    @Autowired CamionRepository camioane;
    @Autowired SoferRepository soferi;
    @Autowired ClientRepository clienti;

    @Test void rb20_codurileSuntUniceSiCrescatoare() {
        String a = coduri.urmatorulCod("TST"), b = coduri.urmatorulCod("TST");
        assertNotEquals(a, b);
        assertTrue(a.matches("TST-\\d{4}-\\d{4}"));
        assertEquals("TST-" + LocalDate.now().getYear() + "-0001", a);
        assertEquals("TST-" + LocalDate.now().getYear() + "-0002", b);
    }

    @Test void rb20_fiecarePrefixAreContorulLui() {
        coduri.urmatorulCod("AAA");
        assertTrue(coduri.urmatorulCod("BBB").endsWith("-0001"));
    }

    @Test void fabricaRutaCreeazaCursaPlanificata() {
        Camion camion = PersistentData.camion(camioane, "IS12ABC", "20000");
        Sofer sofer = PersistentData.sofer(soferi, "1", LocalDate.now().plusYears(1));
        LocalDateTime t = LocalDateTime.now().plusDays(1);
        Ruta r = rutaFactory.creeaza(camion.getId(), sofer.getId(), TestData.IASI, TestData.MUNCHEN, new IntervalTimp(t, t.plusDays(1)));
        assertEquals(StatusRuta.PLANIFICATA, r.getStatus());
        assertTrue(r.getCodRuta().startsWith("RT-"));
        assertNull(r.getFoaieParcurs());
    }

    @Test void fabricaRutaRefuzaCamionSauSoferInexistent() {
        LocalDateTime t = LocalDateTime.now().plusDays(1);
        IntervalTimp interval = new IntervalTimp(t, t.plusDays(1));
        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> rutaFactory.creeaza(999_999L, 999_998L, TestData.IASI, TestData.MUNCHEN, interval));
        assertTrue(ex.hasCode("RB-18"));
    }

    @Test void fabricaComandaAsambleazaAgregatulSiCalculeazaLiniile() {
        Client client = PersistentData.client(clienti, "RO12345678", "0", Moneda.EUR);
        ComandaTransport c = PersistentData.comandaNoua(comandaFactory, client.getId(), "1000", Moneda.EUR, 4, "25");
        assertEquals(StatusComanda.NOUA, c.getStatus());
        assertEquals(1, c.getLinii().size());
        assertSame(c, c.getLinii().get(0).getComanda());                // asociere bidirecțională
        assertEquals(0, new java.math.BigDecimal("100").compareTo(c.getLinii().get(0).getGreutateTotalaKg()));
        assertTrue(c.getNrComanda().startsWith("CMD-"));
    }

    @Test void fabricaComandaRefuzaClientInexistent() {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> PersistentData.comandaNoua(comandaFactory, 999_999L, "1000", Moneda.EUR, 4, "25"));
        assertTrue(ex.hasCode("RB-16"));
    }
}
