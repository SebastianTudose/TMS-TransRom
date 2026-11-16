package tms.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
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
import tms.domain.support.ComandaTransportFactory;
import tms.domain.support.RutaFactory;
import tms.domain.vo.IntervalTimp;
import tms.domain.vo.SumaMonetara;
import tms.persistence.repository.*;
import tms.service.administrare.AdministrareService;
import tms.service.administrare.AutentificareService;
import tms.service.raportare.RaportareService;
import tms.service.workflow.ComandaWorkflowService;
import tms.service.workflow.RutaWorkflowService;
import tms.support.PersistentData;
import tms.support.TestData;

/** UC-01 (autentificare), UC-13..15 (administrare conturi, clienți, camioane) și raportarea (UC-16). */
@SpringJUnitConfig(TmsConfig.class)
@Transactional
class AdministrareSiRaportareTest {

    @Autowired AdministrareService administrare;
    @Autowired AutentificareService autentificare;
    @Autowired RaportareService raportare;
    @Autowired RutaWorkflowService rutaWf;
    @Autowired ComandaWorkflowService comandaWf;
    @Autowired RutaFactory rutaFactory;
    @Autowired ComandaTransportFactory comandaFactory;
    @Autowired UtilizatorRepository utilizatori;
    @Autowired SoferRepository soferi;
    @Autowired ClientRepository clienti;
    @Autowired CamionRepository camioane;

    Administrator admin = TestData.administrator();

    private void asteapta(String cod, Runnable r) {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, r::run);
        assertTrue(ex.hasCode(cod), "Se aștepta " + cod + " dar s-a primit: " + ex.getMessage());
    }

    private Sofer contSofer(String sufix) {
        Sofer s = PersistentData.sofer(soferi, sufix, LocalDate.now().plusYears(1));
        return s;
    }

    @Test void uc13_contNouSalvatSiEmailUnic() {
        Sofer nou = TestData.sofer(0, LocalDate.now().plusYears(1));
        nou.setId(null);
        Utilizator salvat = administrare.creeazaCont(admin, nou);
        assertNotNull(salvat.getId());
        assertTrue(salvat.isActiv());
        Sofer dublura = TestData.sofer(0, LocalDate.now().plusYears(1));
        dublura.setId(null);
        asteapta("RB-01", () -> administrare.creeazaCont(admin, dublura));
    }

    @Test void rb39_doarAdministratorulAdministreaza() {
        Sofer nou = TestData.sofer(0, LocalDate.now().plusYears(1));
        nou.setId(null);
        asteapta("RB-39", () -> administrare.creeazaCont(TestData.dispecer(), nou));
    }

    @Test void rb40_contulDezactivatNuSeAutentifica() {
        Sofer s = contSofer("9");
        assertEquals(s.getId(), autentificare.autentifica("sofer9@tms.ro", "hash").getId());
        administrare.dezactiveazaCont(admin, s.getId());
        assertFalse(s.isActiv());
        asteapta("RB-40", () -> autentificare.autentifica("sofer9@tms.ro", "hash"));
        administrare.reactiveazaCont(admin, s.getId());
        assertDoesNotThrow(() -> autentificare.autentifica("sofer9@tms.ro", "hash"));
    }

    @Test void uc01_credentialeGresite() {
        contSofer("9");
        asteapta("RB-39", () -> autentificare.autentifica("sofer9@tms.ro", "gresit"));
        asteapta("RB-39", () -> autentificare.autentifica("nimeni@tms.ro", "hash"));
    }

    @Test void uc14_clientNouSiCodFiscalUnic() {
        Client c = TestData.client(0, "0", Moneda.EUR);
        c.setId(null);
        c.setCodFiscal("RO555");
        Client salvat = administrare.creeazaClient(admin, c);
        assertNotNull(salvat.getId());
        Client dublura = TestData.client(0, "0", Moneda.EUR);
        dublura.setId(null);
        dublura.setCodFiscal("RO555");
        asteapta("RB-03", () -> administrare.creeazaClient(admin, dublura));
    }

    @Test void uc15_camionIntraSiIeseDinService() {
        Camion c = TestData.camion(0, "IS77XYZ", "10000");
        c.setId(null);
        Camion salvat = administrare.inregistreazaCamion(admin, c);
        assertNotNull(salvat.getId());
        administrare.trimiteInService(admin, salvat.getId());
        assertEquals(StatusCamion.IN_SERVICE, salvat.getStatusTehnic());
        administrare.revineDinService(admin, salvat.getId());
        assertEquals(StatusCamion.DISPONIBIL, salvat.getStatusTehnic());
        Camion dublura = TestData.camion(0, "IS77XYZ", "10000");
        dublura.setId(null);
        asteapta("RB-06", () -> administrare.inregistreazaCamion(admin, dublura));
    }

    @Test void uc16_soferulVedeDoarCurseleLui() {
        Camion camion = PersistentData.camion(camioane, "IS12ABC", "20000");
        Sofer s1 = contSofer("1"), s2 = contSofer("2");
        Client client = PersistentData.client(clienti, "RO1", "0", Moneda.EUR);
        Dispecer d = TestData.dispecer();
        LocalDateTime t = LocalDateTime.now().plusDays(1);
        Ruta r = rutaFactory.creeaza(camion.getId(), s1.getId(), TestData.IASI, TestData.MUNCHEN, new IntervalTimp(t, t.plusDays(1)));
        rutaWf.planificaRuta(d, r, camion, s1);
        assertEquals(1, raportare.curseleMele(s1).size());
        assertTrue(raportare.curseleMele(s2).isEmpty());
    }

    @Test void rb48_centralizatorulEsteRestrictionatPeRol() {
        IntervalTimp azi = new IntervalTimp(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        asteapta("RB-39", () -> raportare.centralizatorConsum(TestData.dispecer(), azi));
        assertDoesNotThrow(() -> raportare.centralizatorConsum(admin, azi));
        assertDoesNotThrow(() -> raportare.centralizatorConsum(TestData.contabil(), azi));
    }
}
