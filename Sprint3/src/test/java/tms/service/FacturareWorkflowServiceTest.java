package tms.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import tms.config.TmsConfig;
import tms.domain.entity.*;
import tms.domain.enums.*;
import tms.domain.exception.BusinessRuleException;
import tms.domain.support.ComandaTransportFactory;
import tms.persistence.repository.ClientRepository;
import tms.persistence.repository.FacturaRepository;
import tms.service.workflow.ComandaWorkflowService;
import tms.service.workflow.FacturareWorkflowService;
import tms.support.PersistentData;
import tms.support.TestData;

/** Testează serviciile de workflow pentru facturare pe repository-uri JPA: emitere, încasare, stornare (RB-35..RB-38). */
@SpringJUnitConfig(TmsConfig.class)
@Transactional
class FacturareWorkflowServiceTest {

    @Autowired FacturareWorkflowService facturare;
    @Autowired ComandaWorkflowService comenziWorkflow;
    @Autowired ComandaTransportFactory comandaFactory;
    @Autowired ClientRepository clienti;
    @Autowired FacturaRepository facturi;

    Dispecer dispecer = TestData.dispecer();
    Contabil contabil = TestData.contabil();
    Client client;
    ComandaTransport c1, c2;

    @BeforeEach
    void pregateste() {
        client = PersistentData.client(clienti, "RO12345678", "10", Moneda.EUR);
        c1 = PersistentData.comanda(comandaFactory, comenziWorkflow, dispecer, client.getId(), "1000", Moneda.EUR, 10, "100");
        c1.setStatus(StatusComanda.LIVRATA);
        c2 = PersistentData.comanda(comandaFactory, comenziWorkflow, dispecer, client.getId(), "500", Moneda.EUR, 5, "40");
        c2.setStatus(StatusComanda.LIVRATA);
    }

    private static void egal(String asteptat, BigDecimal real) {
        assertEquals(0, new BigDecimal(asteptat).compareTo(real), "Se aștepta " + asteptat + " dar s-a obținut " + real);
    }

    private void asteapta(String cod, Runnable actiune) {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, actiune::run);
        assertTrue(ex.hasCode(cod), "Se aștepta " + cod + " dar s-a primit: " + ex.getMessage());
    }

    private LocalDate scadenta() { return LocalDate.now().plusDays(30); }

    @Test void rb36_emitereaFacturiiActualizeazaComenzileSiSoldul() {
        Factura f = facturare.emiteFactura(contabil, client.getId(), List.of(c1.getId(), c2.getId()), scadenta());
        assertNotNull(f.getId());
        egal("1350.00", f.getValoareTotala().valoare());                    // (1000 + 500) - 10%
        assertEquals(Moneda.EUR, f.getMoneda());
        assertEquals(StatusFactura.EMISA, f.getStatus());
        assertEquals(2, f.getLinii().size());
        assertEquals(StatusComanda.FACTURATA, c1.getStatus());
        egal("1350.00", client.getSoldDebitor());
        assertNotNull(f.getNumar());
        assertTrue(facturi.findByNumar(f.getNumar()).isPresent());
    }

    @Test void rb37_incasareaScadeSoldul() {
        Factura f = facturare.emiteFactura(contabil, client.getId(), List.of(c1.getId(), c2.getId()), scadenta());
        facturare.incaseaza(contabil, f);
        assertEquals(StatusFactura.INCASATA, f.getStatus());
        egal("0", client.getSoldDebitor());
        asteapta("RB-37", () -> facturare.incaseaza(contabil, f));      // nu se poate încasa de două ori
    }

    @Test void rb38_stornareaRedeschideComenzileFaraStergereaFacturii() {
        Factura f = facturare.emiteFactura(contabil, client.getId(), List.of(c1.getId(), c2.getId()), scadenta());
        facturare.storneaza(contabil, f);
        assertEquals(StatusFactura.STORNATA, f.getStatus());
        assertEquals(StatusComanda.LIVRATA, c1.getStatus());
        egal("0", client.getSoldDebitor());
        assertEquals(2, f.getLinii().size());                                   // documentul se păstrează
        assertTrue(facturi.findById(f.getId()).isPresent());
        asteapta("RB-38", () -> facturare.storneaza(contabil, f));
    }

    @Test void rb35_comandaNelivrataNuSeFactureaza() {
        c1.setStatus(StatusComanda.IN_EXECUTIE);
        asteapta("RB-35", () -> facturare.emiteFactura(contabil, client.getId(), List.of(c1.getId()), scadenta()));
        egal("0", client.getSoldDebitor());
    }

    @Test void rb35_comandaNuSeFactureazaDeDouaOri() {
        facturare.emiteFactura(contabil, client.getId(), List.of(c1.getId()), scadenta());
        asteapta("RB-35", () -> facturare.emiteFactura(contabil, client.getId(), List.of(c1.getId()), scadenta()));
    }

    @Test void rb35_comandaAltuiClientEsteRefuzata() {
        Client altul = PersistentData.client(clienti, "RO87654321", "0", Moneda.EUR);
        asteapta("RB-35", () -> facturare.emiteFactura(contabil, altul.getId(), List.of(c1.getId()), scadenta()));
    }

    @Test void rb35_listaGoalaSauCuDuplicateEsteRefuzata() {
        asteapta("RB-35", () -> facturare.emiteFactura(contabil, client.getId(), List.of(), scadenta()));
        asteapta("RB-35", () -> facturare.emiteFactura(contabil, client.getId(), List.of(c1.getId(), c1.getId()), scadenta()));
    }

    @Test void rb17_monedaComenziiTrebuieSaCorespundaClientului() {
        ComandaTransport inRon = PersistentData.comanda(comandaFactory, comenziWorkflow, dispecer, client.getId(), "4000", Moneda.RON, 1, "10");
        inRon.setStatus(StatusComanda.LIVRATA);
        asteapta("RB-17", () -> facturare.emiteFactura(contabil, client.getId(), List.of(inRon.getId()), scadenta()));
        assertEquals(StatusComanda.LIVRATA, inRon.getStatus());
    }

    @Test void rb39_doarContabilulFactureaza() {
        asteapta("RB-39", () -> facturare.emiteFactura(dispecer, client.getId(), List.of(c1.getId()), scadenta()));
    }
}
