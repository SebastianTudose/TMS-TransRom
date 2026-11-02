package tms.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import tms.config.TmsConfig;
import tms.domain.entity.*;
import tms.domain.enums.*;
import tms.domain.exception.BusinessRuleException;
import tms.domain.support.ComandaTransportFactory;
import tms.service.workflow.FacturareWorkflowService;
import tms.support.TestData;

/** Testează serviciile de workflow pentru facturare: emitere, încasare, stornare (RB-35..RB-38). */
@SpringJUnitConfig(TmsConfig.class)
class FacturareWorkflowServiceTest {

    @Autowired FacturareWorkflowService facturare;
    @Autowired ComandaTransportFactory comandaFactory;

    Contabil contabil = TestData.contabil();
    Client client;
    ComandaTransport c1, c2;

    @BeforeEach
    void pregateste() {
        client = TestData.client(5, "10", Moneda.EUR);
        c1 = TestData.comanda(comandaFactory, 100, 5, "1000", Moneda.EUR, 10, "100"); c1.setStatus(StatusComanda.LIVRATA);
        c2 = TestData.comanda(comandaFactory, 101, 5, "500", Moneda.EUR, 5, "40");    c2.setStatus(StatusComanda.LIVRATA);
    }

    private static void egal(String asteptat, BigDecimal real) {
        assertEquals(0, new BigDecimal(asteptat).compareTo(real), "Se aștepta " + asteptat + " dar s-a obținut " + real);
    }

    private void asteapta(String cod, Runnable actiune) {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, actiune::run);
        assertTrue(ex.hasCode(cod), "Se aștepta " + cod + " dar s-a primit: " + ex.getMessage());
    }

    @Test void rb36_emitereaFacturiiActualizeazaComenzileSiSoldul() {
        Factura f = facturare.emiteFactura(contabil, client, List.of(c1, c2), LocalDate.now().plusDays(30));
        egal("1350.00", f.getValoareTotala().valoare());                    // (1000 + 500) - 10%
        assertEquals(Moneda.EUR, f.getMoneda());
        assertEquals(StatusFactura.EMISA, f.getStatus());
        assertEquals(2, f.getLinii().size());
        assertEquals(StatusComanda.FACTURATA, c1.getStatus());
        egal("1350.00", client.getSoldDebitor());
        assertNotNull(f.getNumar());
    }

    @Test void rb37_incasareaScadeSoldul() {
        Factura f = facturare.emiteFactura(contabil, client, List.of(c1, c2), LocalDate.now().plusDays(30));
        facturare.incaseaza(contabil, client, f);
        assertEquals(StatusFactura.INCASATA, f.getStatus());
        egal("0", client.getSoldDebitor());
        asteapta("RB-37", () -> facturare.incaseaza(contabil, client, f));      // nu se poate încasa de două ori
    }

    @Test void rb38_stornareaRedeschideComenzileFaraStergereaFacturii() {
        Factura f = facturare.emiteFactura(contabil, client, List.of(c1, c2), LocalDate.now().plusDays(30));
        facturare.storneaza(contabil, client, f, List.of(c1, c2));
        assertEquals(StatusFactura.STORNATA, f.getStatus());
        assertEquals(StatusComanda.LIVRATA, c1.getStatus());
        egal("0", client.getSoldDebitor());
        assertEquals(2, f.getLinii().size());                                   // documentul se păstrează
        asteapta("RB-38", () -> facturare.storneaza(contabil, client, f, List.of(c1, c2)));
    }

    @Test void rb35_comandaNelivrataNuSeFactureaza() {
        c1.setStatus(StatusComanda.IN_EXECUTIE);
        asteapta("RB-35", () -> facturare.emiteFactura(contabil, client, List.of(c1), LocalDate.now().plusDays(30)));
        egal("0", client.getSoldDebitor());
    }

    @Test void rb35_comandaNuSeFactureazaDeDouaOri() {
        facturare.emiteFactura(contabil, client, List.of(c1), LocalDate.now().plusDays(30));
        asteapta("RB-35", () -> facturare.emiteFactura(contabil, client, List.of(c1), LocalDate.now().plusDays(30)));
    }

    @Test void rb35_comandaAltuiClientEsteRefuzata() {
        Client altul = TestData.client(6, "0", Moneda.EUR);
        asteapta("RB-35", () -> facturare.emiteFactura(contabil, altul, List.of(c1), LocalDate.now().plusDays(30)));
    }

    @Test void rb17_monedaComenziiTrebuieSaCorespundaClientului() {
        ComandaTransport inRon = TestData.comanda(comandaFactory, 102, 5, "4000", Moneda.RON, 1, "10");
        inRon.setStatus(StatusComanda.LIVRATA);
        asteapta("RB-17", () -> facturare.emiteFactura(contabil, client, List.of(inRon), LocalDate.now().plusDays(30)));
        assertEquals(StatusComanda.LIVRATA, inRon.getStatus());
    }

    @Test void rb39_doarContabilulFactureaza() {
        asteapta("RB-39", () -> facturare.emiteFactura(TestData.dispecer(), client, List.of(c1), LocalDate.now().plusDays(30)));
    }
}
