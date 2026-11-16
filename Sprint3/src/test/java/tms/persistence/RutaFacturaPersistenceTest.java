package tms.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tms.domain.entity.*;
import tms.domain.enums.*;
import tms.domain.support.ComandaTransportFactory;
import tms.domain.support.RutaFactory;
import tms.domain.vo.IntervalTimp;
import tms.domain.vo.SumaMonetara;
import tms.persistence.repository.*;
import tms.service.workflow.ComandaWorkflowService;
import tms.service.workflow.FacturareWorkflowService;
import tms.service.workflow.RutaWorkflowService;
import tms.support.PersistentData;
import tms.support.TestData;

/** Cascade, orphanRemoval, interogări @Query și listeneri de calcul/audit/protecție, pe agregatele Ruta și Factura. */
class RutaFacturaPersistenceTest extends PersistenceTestBase {

    @Autowired RutaWorkflowService rutaWf;
    @Autowired ComandaWorkflowService comandaWf;
    @Autowired FacturareWorkflowService facturareWf;
    @Autowired RutaFactory rutaFactory;
    @Autowired ComandaTransportFactory comandaFactory;
    @Autowired CamionRepository camioane;
    @Autowired SoferRepository soferi;
    @Autowired ClientRepository clienti;
    @Autowired ComandaTransportRepository comenzi;
    @Autowired RutaRepository rute;
    @Autowired FacturaRepository facturi;
    @Autowired CmrRepository cmrUri;

    Dispecer dispecer = TestData.dispecer();
    Contabil contabil = TestData.contabil();
    Camion camion; Sofer sofer; Client client; Ruta ruta; ComandaTransport comanda;

    @BeforeEach
    void pregateste() {
        camion = PersistentData.camion(camioane, "IS12ABC", "20000");
        sofer = PersistentData.sofer(soferi, "1", LocalDate.now().plusYears(2));
        client = PersistentData.client(clienti, "RO12345678", "10", Moneda.EUR);
        comanda = PersistentData.comanda(comandaFactory, comandaWf, dispecer, client.getId(), "1000", Moneda.EUR, 10, "100");
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        ruta = rutaFactory.creeaza(camion.getId(), sofer.getId(), TestData.IASI, TestData.MUNCHEN, new IntervalTimp(start, start.plusDays(2)));
        ruta = rutaWf.planificaRuta(dispecer, ruta, camion, sofer);
    }

    private void livreaza() {
        rutaWf.alocaComanda(dispecer, ruta, comanda);
        rutaWf.pornesteRuta(dispecer, ruta, new BigDecimal("1000"));
        rutaWf.finalizeazaRuta(dispecer, ruta, new BigDecimal("1350"), new BigDecimal("52"), SumaMonetara.of("7.85", Moneda.RON));
    }

    @Test void comandaSePersistaCuLiniileEiPrinCascade() {
        Long id = comanda.getId();
        sincronizeaza();
        ComandaTransport citita = comenzi.findCuLinii(id).orElseThrow();
        assertEquals(1, citita.getLinii().size());
        assertEquals(0, new BigDecimal("1000").compareTo(citita.getLinii().get(0).getGreutateTotalaKg()));   // RB-44 calculat de listener
        assertEquals(0, new BigDecimal("1000").compareTo(citita.getTarifAgreat().valoare()));              // embeddable SumaMonetara
        assertEquals(Moneda.EUR, citita.getTarifAgreat().moneda());
        assertTrue(comenzi.findByNrComanda(citita.getNrComanda()).isPresent());
    }

    @Test void liniaEliminataDinAgregatSeSterge() {
        comanda.addLinie(comandaFactory.creeazaLinie("Cutii", 2, new BigDecimal("5"), new BigDecimal("0.1")));
        sincronizeaza();
        ComandaTransport c = comenzi.findCuLinii(comanda.getId()).orElseThrow();
        assertEquals(2, c.getLinii().size());
        c.removeLinie(c.getLinii().get(1));
        sincronizeaza();
        assertEquals(1, comenzi.findCuLinii(comanda.getId()).orElseThrow().getLinii().size());
    }

    @Test void comenziDupaStatusSiClient() {
        sincronizeaza();
        assertEquals(1, comenzi.findByStatus(StatusComanda.NOUA).size());
        assertEquals(1, comenzi.findByClientIdAndStatus(client.getId(), StatusComanda.NOUA).size());
        assertTrue(comenzi.findByClientIdAndStatus(client.getId(), StatusComanda.LIVRATA).isEmpty());
    }

    @Test void interogareaDeSuprapunereGasesteCursaActiva() {
        Set<StatusRuta> active = Set.of(StatusRuta.PLANIFICATA, StatusRuta.IN_TRANZIT);
        IntervalTimp i = ruta.getIntervalPlanificat();
        sincronizeaza();
        assertEquals(1, rute.findSuprapuse(active, camion.getId(), -1L, i.inceput().plusHours(1), i.sfarsit().plusHours(1)).size());
        assertEquals(1, rute.findSuprapuse(active, -1L, sofer.getId(), i.inceput().plusHours(1), i.sfarsit().plusHours(1)).size());
        assertTrue(rute.findSuprapuse(active, camion.getId(), sofer.getId(), i.sfarsit().plusHours(1), i.sfarsit().plusDays(1)).isEmpty());
    }

    @Test void alocareaSalveazaCmrSiAlocarea() {
        Cmr cmr = rutaWf.alocaComanda(dispecer, ruta, comanda);
        sincronizeaza();
        assertEquals(1, cmrUri.findByRutaId(ruta.getId()).size());
        assertEquals(1, cmrUri.findByComandaId(comanda.getId()).size());
        assertTrue(cmrUri.findByNumarCmr(cmr.getNumarCmr()).isPresent());
        assertEquals(1, rute.findById(ruta.getId()).orElseThrow().getAlocari().size());
    }

    @Test void finalizareaPersistaFoaiaSiCostulCalculatDeListener() {
        livreaza();
        Long id = ruta.getId();
        sincronizeaza();
        Ruta r = rute.findById(id).orElseThrow();
        assertNotNull(r.getFoaieParcurs());
        assertNotNull(r.getCostCombustibil());
        assertEquals(0, new BigDecimal("408.20").compareTo(r.getCostCombustibil().getCostTotal().valoare()));   // RB-43
        LocalDateTime acum = LocalDateTime.now();
        assertEquals(1, rute.findCuCostInPerioada(StatusRuta.FINALIZATA, acum.minusHours(1), acum.plusHours(1)).size());
        assertTrue(rute.findCuCostInPerioada(StatusRuta.ANULATA, acum.minusHours(1), acum.plusHours(1)).isEmpty());
    }

    @Test void auditulSeCompleteazaLaCreareSiLaModificare() {
        assertEquals("dispecer@tms.ro", ruta.getCreatedByUser());
        Integer v = ruta.getVersion();
        rutaWf.alocaComanda(dispecer, ruta, comanda);
        sincronizeaza();
        Ruta r = rute.findById(ruta.getId()).orElseThrow();
        assertEquals("dispecer@tms.ro", r.getUpdatedByUser());
        assertNotNull(r.getDateUpdated());
        assertTrue(r.getVersion() >= v);
    }

    @Test void facturaSePersistaCuLiniileSiTotalulCalculatDeListener() {
        livreaza();
        comanda.setStatus(StatusComanda.LIVRATA);
        Factura f = facturareWf.emiteFactura(contabil, client.getId(), List.of(comanda.getId()), LocalDate.now().minusDays(1));
        Long id = f.getId();
        sincronizeaza();
        Factura citita = facturi.findCuLinii(id).orElseThrow();
        assertEquals(1, citita.getLinii().size());
        assertEquals(0, new BigDecimal("900.00").compareTo(citita.getValoareTotala().valoare()));     // 1000 - 10% (RB-45)
        assertTrue(facturi.findByNumar(citita.getNumar()).isPresent());
        assertEquals(1, facturi.findByClientId(client.getId()).size());
        assertEquals(1, facturi.findByClientIdAndStatus(client.getId(), StatusFactura.EMISA).size());
        assertEquals(1, facturi.findByComandaId(comanda.getId()).size());
        assertEquals(1, facturi.findScadente(StatusFactura.EMISA, LocalDate.now()).size());
        assertTrue(facturi.findScadente(StatusFactura.INCASATA, LocalDate.now()).isEmpty());
    }

    @Test void documenteleNuSeStergFizic() {
        Cmr cmr = rutaWf.alocaComanda(dispecer, ruta, comanda);
        sincronizeaza();
        Exception e = assertThrows(Exception.class, () -> { em.remove(em.find(Ruta.class, ruta.getId())); em.flush(); });
        assertTrue(radacina(e) instanceof tms.domain.exception.BusinessRuleException br && br.hasCode("RB-33"), radacina(e).toString());
    }

    @Test void cmrulNuSeStergeFizic() {
        Cmr cmr = rutaWf.alocaComanda(dispecer, ruta, comanda);
        sincronizeaza();
        Long id = cmr.getId();
        Exception e = assertThrows(Exception.class, () -> { em.remove(em.find(Cmr.class, id)); em.flush(); });
        assertTrue(radacina(e) instanceof tms.domain.exception.BusinessRuleException br && br.hasCode("RB-33"), radacina(e).toString());
    }
}
