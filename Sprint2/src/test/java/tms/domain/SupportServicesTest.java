package tms.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import tms.config.TmsConfig;
import tms.domain.entity.*;
import tms.domain.enums.*;
import tms.domain.support.*;
import tms.domain.vo.IntervalTimp;
import tms.support.TestData;

/** Testează serviciile de suport ale entităților: secvențe de coduri și fabricile de agregate. */
@SpringJUnitConfig(TmsConfig.class)
class SupportServicesTest {

    @Autowired SecventaCoduriService coduri;
    @Autowired RutaFactory rutaFactory;
    @Autowired ComandaTransportFactory comandaFactory;

    @Test void rb20_codurileSuntUniceSiCrescatoare() {
        String a = coduri.urmatorulCod("TST"), b = coduri.urmatorulCod("TST");
        assertNotEquals(a, b);
        assertTrue(a.matches("TST-\\d{4}-\\d{4}"));
        assertEquals("TST-" + LocalDate.now().getYear() + "-0001", a);
    }

    @Test void fabricaRutaCreeazaCursaPlanificata() {
        LocalDateTime t = LocalDateTime.now().plusDays(1);
        Ruta r = rutaFactory.creeaza(1L, 2L, TestData.IASI, TestData.MUNCHEN, new IntervalTimp(t, t.plusDays(1)));
        assertEquals(StatusRuta.PLANIFICATA, r.getStatus());
        assertTrue(r.getCodRuta().startsWith("RT-"));
        assertNull(r.getFoaieParcurs());
    }

    @Test void fabricaComandaAsambleazaAgregatulSiCalculeazaLiniile() {
        ComandaTransport c = TestData.comanda(comandaFactory, 1, 5, "1000", Moneda.EUR, 4, "25");
        assertEquals(StatusComanda.NOUA, c.getStatus());
        assertEquals(1, c.getLinii().size());
        assertSame(c, c.getLinii().get(0).getComanda());                // asociere bidirecțională
        assertEquals(0, new java.math.BigDecimal("100").compareTo(c.getLinii().get(0).getGreutateTotalaKg()));
        assertTrue(c.getNrComanda().startsWith("CMD-"));
    }
}
