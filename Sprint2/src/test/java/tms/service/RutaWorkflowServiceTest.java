package tms.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import tms.domain.support.RutaFactory;
import tms.domain.vo.*;
import tms.service.audit.AuditService;
import tms.service.workflow.ComandaWorkflowService;
import tms.service.workflow.RutaWorkflowService;
import tms.support.TestData;

/** Testează serviciile de workflow ale cursei: planificare, alocare + CMR, pornire, finalizare, corectare, anulări. */
@SpringJUnitConfig(TmsConfig.class)
class RutaWorkflowServiceTest {

    @Autowired RutaWorkflowService workflow;
    @Autowired ComandaWorkflowService comenziWorkflow;
    @Autowired RutaFactory rutaFactory;
    @Autowired ComandaTransportFactory comandaFactory;
    @Autowired AuditService audit;

    private static final java.util.concurrent.atomic.AtomicLong SECVENTA_ID = new java.util.concurrent.atomic.AtomicLong(1000);

    Dispecer dispecer = TestData.dispecer();
    Camion camion;
    Sofer sofer;
    Ruta ruta;
    ComandaTransport comanda1, comanda2;

    @BeforeEach
    void pregateste() {
        camion = TestData.camion(10, "IS12ABC", "20000");
        sofer = TestData.sofer(20, LocalDate.now().plusYears(2));
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        ruta = rutaFactory.creeaza(10L, 20L, TestData.IASI, TestData.MUNCHEN, new IntervalTimp(start, start.plusDays(2)));
        ruta.setId(SECVENTA_ID.incrementAndGet());   // id unic: auditul este partajat între teste
        comanda1 = TestData.comanda(comandaFactory, 100, 5, "1000", Moneda.EUR, 10, "120.5");   // 1205 kg
        comanda2 = TestData.comanda(comandaFactory, 101, 5, "500", Moneda.EUR, 5, "40");        // 200 kg
        comenziWorkflow.creeazaComanda(dispecer, comanda1);
        comenziWorkflow.creeazaComanda(dispecer, comanda2);
    }

    private static void egal(String asteptat, BigDecimal real) {
        assertEquals(0, new BigDecimal(asteptat).compareTo(real), "Se aștepta " + asteptat + " dar s-a obținut " + real);
    }

    private void asteapta(String cod, Runnable actiune) {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, actiune::run);
        assertTrue(ex.hasCode(cod), "Se aștepta " + cod + " dar s-a primit: " + ex.getMessage());
    }

    private List<Cmr> planificaAlocaPorneste() {
        workflow.planificaRuta(dispecer, ruta, camion, sofer, List.of());
        Cmr cmr1 = workflow.alocaComanda(dispecer, ruta, comanda1, camion);
        Cmr cmr2 = workflow.alocaComanda(dispecer, ruta, comanda2, camion);
        workflow.pornesteRuta(dispecer, ruta, camion, List.of(comanda1, comanda2), new BigDecimal("120000"));
        return List.of(cmr1, cmr2);
    }

    private void finalizeaza() {
        workflow.finalizeazaRuta(dispecer, ruta, camion, List.of(comanda1, comanda2),
                new BigDecimal("120350"), new BigDecimal("52.0"), SumaMonetara.of("7.85", Moneda.RON));
    }

    @Test void uc03_planificareCompleteazaAuditul() {
        workflow.planificaRuta(dispecer, ruta, camion, sofer, List.of());
        assertEquals(StatusRuta.PLANIFICATA, ruta.getStatus());
        assertEquals("dispecer@tms.ro", ruta.getCreatedByUser());
        assertEquals(1, audit.istoric(ruta).size());
    }

    @Test void uc04_alocareaEmiteCmrSiPlanificaComanda() {
        workflow.planificaRuta(dispecer, ruta, camion, sofer, List.of());
        Cmr cmr = workflow.alocaComanda(dispecer, ruta, comanda1, camion);
        assertEquals(StatusCmr.EMIS, cmr.getStatus());
        egal("1205", cmr.getGreutateBruta());
        assertEquals(StatusComanda.PLANIFICATA, comanda1.getStatus());
        assertEquals(1, ruta.getAlocari().size());
        assertNotNull(cmr.getNumarCmr());
    }

    @Test void rb19_alocareaPesteCapacitateEsteRefuzata() {
        workflow.planificaRuta(dispecer, ruta, camion, sofer, List.of());
        workflow.alocaComanda(dispecer, ruta, comanda1, camion);
        ComandaTransport grea = TestData.comanda(comandaFactory, 102, 5, "900", Moneda.EUR, 100, "190");   // 19 000 kg
        comenziWorkflow.creeazaComanda(dispecer, grea);
        asteapta("RB-19", () -> workflow.alocaComanda(dispecer, ruta, grea, camion));
        assertEquals(StatusComanda.NOUA, grea.getStatus());       // nicio modificare parțială
        assertEquals(1, ruta.getAlocari().size());
    }

    @Test void rb27_dealocareaAnuleazaCmrulSiPastreazaIstoricul() {
        workflow.planificaRuta(dispecer, ruta, camion, sofer, List.of());
        Cmr cmr = workflow.alocaComanda(dispecer, ruta, comanda1, camion);
        workflow.dealocaComanda(dispecer, ruta, comanda1, cmr);
        assertEquals(StatusCmr.ANULAT, cmr.getStatus());
        assertEquals(StatusComanda.NOUA, comanda1.getStatus());
        assertTrue(ruta.getAlocari().isEmpty());
    }

    @Test void uc06_uc07_cicluComplet() {
        planificaAlocaPorneste();
        assertEquals(StatusRuta.IN_TRANZIT, ruta.getStatus());
        assertEquals(StatusCamion.IN_CURSA, camion.getStatusTehnic());
        assertEquals(StatusComanda.IN_EXECUTIE, comanda1.getStatus());
        assertEquals(StatusDocument.INTOCMIT, ruta.getFoaieParcurs().getStatus());

        finalizeaza();
        assertEquals(StatusRuta.FINALIZATA, ruta.getStatus());
        egal("350", ruta.getDistantaParcursa());                                  // RB-42
        egal("408.20", ruta.getCostCombustibil().getCostTotal().valoare());      // RB-43
        assertEquals(StatusCamion.DISPONIBIL, camion.getStatusTehnic());
        assertEquals(StatusDocument.APROBAT, ruta.getFoaieParcurs().getStatus());
        assertEquals(StatusComanda.LIVRATA, comanda1.getStatus());
        assertEquals(StatusComanda.LIVRATA, comanda2.getStatus());
        assertNotNull(ruta.getDataSosireReala());
    }

    @Test void rb13_kmInvalidiNuModificaNimic() {
        planificaAlocaPorneste();
        asteapta("RB-13", () -> workflow.finalizeazaRuta(dispecer, ruta, camion, List.of(comanda1, comanda2),
                new BigDecimal("119000"), new BigDecimal("52.0"), SumaMonetara.of("7.85", Moneda.RON)));
        assertEquals(StatusRuta.IN_TRANZIT, ruta.getStatus());
        assertNull(ruta.getCostCombustibil());
        assertNull(ruta.getFoaieParcurs().getKmSosire());
        assertEquals(StatusCamion.IN_CURSA, camion.getStatusTehnic());
    }

    @Test void rb29_nuSePoateFinalizaOCursaPlanificata() {
        workflow.planificaRuta(dispecer, ruta, camion, sofer, List.of());
        workflow.alocaComanda(dispecer, ruta, comanda1, camion);
        asteapta("RB-29", () -> workflow.finalizeazaRuta(dispecer, ruta, camion, List.of(comanda1),
                new BigDecimal("1"), BigDecimal.ONE, SumaMonetara.of("7.85", Moneda.RON)));
    }

    @Test void rb30_corectareaRecalculeazaDistantaSiCostul() {
        planificaAlocaPorneste(); finalizeaza();
        workflow.corecteazaFinalizare(dispecer, ruta, List.of(comanda1, comanda2),
                new BigDecimal("120400"), new BigDecimal("50.0"), SumaMonetara.of("8.00", Moneda.RON));
        egal("400", ruta.getDistantaParcursa());
        egal("400.00", ruta.getCostCombustibil().getCostTotal().valoare());
        assertEquals(StatusRuta.FINALIZATA, ruta.getStatus());
    }

    @Test void rb31_anulareaFinalizariiRestaureazaStarile() {
        planificaAlocaPorneste(); finalizeaza();
        workflow.anuleazaFinalizarea(dispecer, ruta, camion, List.of(comanda1, comanda2));
        assertEquals(StatusRuta.IN_TRANZIT, ruta.getStatus());
        assertNull(ruta.getCostCombustibil());
        assertNull(ruta.getFoaieParcurs().getKmSosire());
        assertEquals(StatusDocument.INTOCMIT, ruta.getFoaieParcurs().getStatus());
        egal("0", ruta.getDistantaParcursa());
        assertEquals(StatusCamion.IN_CURSA, camion.getStatusTehnic());
        assertEquals(StatusComanda.IN_EXECUTIE, comanda1.getStatus());
    }

    @Test void rb31_anulareaEsteRefuzataDacaCamionulSaRealocat() {
        planificaAlocaPorneste(); finalizeaza();
        camion.setStatusTehnic(StatusCamion.IN_CURSA);                       // camion pornit pe altă cursă
        asteapta("RB-31", () -> workflow.anuleazaFinalizarea(dispecer, ruta, camion, List.of(comanda1, comanda2)));
        assertEquals(StatusRuta.FINALIZATA, ruta.getStatus());
    }

    @Test void rb32_anulareaCurseiPlanificate() {
        workflow.planificaRuta(dispecer, ruta, camion, sofer, List.of());
        Cmr cmr = workflow.alocaComanda(dispecer, ruta, comanda1, camion);
        workflow.anuleazaRuta(dispecer, ruta, camion, List.of(comanda1), List.of(cmr));
        assertEquals(StatusRuta.ANULATA, ruta.getStatus());
        assertEquals(StatusCmr.ANULAT, cmr.getStatus());
        assertEquals(StatusComanda.NOUA, comanda1.getStatus());
        assertEquals(StatusCamion.DISPONIBIL, camion.getStatusTehnic());
    }

    @Test void rb32_anulareaCurseiInTranzitElibereazaCamionul() {
        List<Cmr> cmrUri = planificaAlocaPorneste();
        workflow.anuleazaRuta(dispecer, ruta, camion, List.of(comanda1, comanda2), cmrUri);
        assertEquals(StatusRuta.ANULATA, ruta.getStatus());
        assertEquals(StatusCamion.DISPONIBIL, camion.getStatusTehnic());
        assertEquals(StatusDocument.ANULAT, ruta.getFoaieParcurs().getStatus());
        assertEquals(StatusComanda.NOUA, comanda2.getStatus());
    }

    @Test void rb32_ocursaFinalizataNuSePoateAnula() {
        planificaAlocaPorneste(); finalizeaza();
        asteapta("RB-32", () -> workflow.anuleazaRuta(dispecer, ruta, camion, List.of(comanda1, comanda2), List.of()));
    }

    @Test void rb22_oaDouaCursaSuprapusaEsteRefuzata() {
        workflow.planificaRuta(dispecer, ruta, camion, sofer, List.of());
        Ruta alta = rutaFactory.creeaza(10L, 20L, TestData.IASI, TestData.MUNCHEN,
                new IntervalTimp(ruta.getIntervalPlanificat().inceput().plusDays(1), ruta.getIntervalPlanificat().sfarsit().plusDays(1)));
        alta.setId(ruta.getId() + 5000);
        asteapta("RB-22", () -> workflow.planificaRuta(dispecer, alta, camion, sofer, List.of(ruta)));
    }

    @Test void rb39_doarDispecerulPlanifica() {
        asteapta("RB-39", () -> workflow.planificaRuta(TestData.contabil(), ruta, camion, sofer, List.of()));
        asteapta("RB-39", () -> workflow.planificaRuta(TestData.administrator(), ruta, camion, sofer, List.of()));
    }

    @Test void rb34_comandaAnulataNuMaiPoateFiAlocata() {
        comenziWorkflow.anuleazaComanda(dispecer, comanda1);
        workflow.planificaRuta(dispecer, ruta, camion, sofer, List.of());
        asteapta("RB-25", () -> workflow.alocaComanda(dispecer, ruta, comanda1, camion));
        asteapta("RB-34", () -> comenziWorkflow.modificaComanda(dispecer, comanda1));
    }

    @Test void rb41_operatiileSuntAuditate() {
        planificaAlocaPorneste(); finalizeaza();
        assertTrue(audit.istoric(ruta).size() >= 4);              // creare + alocări + pornire + finalizare
        assertTrue(audit.istoric(camion).stream().anyMatch(e -> "Status DISPONIBIL".equals(e.detalii())));
        assertEquals("dispecer@tms.ro", ruta.getUpdatedByUser());
    }
}
