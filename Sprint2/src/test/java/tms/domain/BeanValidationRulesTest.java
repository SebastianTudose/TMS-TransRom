package tms.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import tms.config.TmsConfig;
import tms.domain.entity.*;
import tms.domain.enums.Moneda;
import tms.domain.exception.BusinessRuleException;
import tms.domain.vo.*;
import tms.service.validation.ValidationService;
import tms.support.TestData;

/** Testează regulile RB-01..RB-17 declarate prin adnotări JavaBean Validation în entități. */
@SpringJUnitConfig(TmsConfig.class)
class BeanValidationRulesTest {

    @Autowired ValidationService validare;

    private void asteaptaRegula(String cod, Object entitate) {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> validare.valideazaEntitate(entitate));
        assertTrue(ex.hasCode(cod), "Se aștepta " + cod + " dar s-a primit: " + ex.getMessage());
    }

    @Test void entitateValidaNuAruncaExceptii() {
        assertDoesNotThrow(() -> validare.valideazaEntitate(TestData.dispecer()));
        assertDoesNotThrow(() -> validare.valideazaEntitate(TestData.client(1, "5", Moneda.EUR)));
        assertDoesNotThrow(() -> validare.valideazaEntitate(TestData.camion(1, "IS12ABC", "20000")));
    }

    @Test void rb01_emailInvalid() {
        Dispecer d = TestData.dispecer(); d.setEmail(new AdresaEmail("nu-este-email"));
        asteaptaRegula("RB-01", d);
    }

    @Test void rb02_contFaraNume() {
        Dispecer d = TestData.dispecer(); d.setNume(" ");
        asteaptaRegula("RB-02", d);
    }

    @Test void rb03_rb04_rb05_client() {
        Client c = TestData.client(1, "150", Moneda.EUR);
        c.setCodFiscal("123"); c.setContBancar(new Iban("RO49AAAA"));
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> validare.valideazaEntitate(c));
        assertTrue(ex.hasCode("RB-03")); assertTrue(ex.hasCode("RB-04")); assertTrue(ex.hasCode("RB-05"));
    }

    @Test void rb06_rb07_camion() {
        Camion c = TestData.camion(1, "1234", "0");
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> validare.valideazaEntitate(c));
        assertTrue(ex.hasCode("RB-06")); assertTrue(ex.hasCode("RB-07"));
    }

    @Test void rb08_serieSofer() {
        Sofer s = TestData.sofer(1, LocalDate.now().plusYears(1)); s.setSeriePermis("ab");
        asteaptaRegula("RB-08", s);
    }

    @Test void rb09_intervalInvalid() {
        IntervalTimp i = new IntervalTimp(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1));
        asteaptaRegula("RB-09", i);
    }

    @Test void rb10_sumaNegativa() { asteaptaRegula("RB-10", new SumaMonetara(new BigDecimal("-5"), Moneda.EUR)); }

    @Test void rb11_tara() { asteaptaRegula("RB-11", new Locatie("Romania", "Iași", null)); }

    @Test void rb12_linieComanda() {
        LinieComanda l = new LinieComanda();
        l.setDescriereMarfa("Paleți"); l.setCantitate(0);
        l.setGreutateUnitaraKg(BigDecimal.ONE); l.setVolumUnitarM3(BigDecimal.ONE);
        asteaptaRegula("RB-12", l);
    }

    @Test void rb13_kmSosireMaiMicDecatPlecare() {
        FoaieParcurs f = new FoaieParcurs();
        f.setKmPlecare(new BigDecimal("1000")); f.setKmSosire(new BigDecimal("900"));
        asteaptaRegula("RB-13", f);
    }

    @Test void rb14_consumNegativ() {
        CostCombustibil c = new CostCombustibil();
        c.setConsumTotal(new BigDecimal("-1")); c.setPretPerLitru(SumaMonetara.of("7.85", Moneda.RON));
        asteaptaRegula("RB-14", c);
    }

    @Test void rb15_greutateBrutaZero() {
        Cmr c = new Cmr(); c.setRutaId(1L); c.setComandaId(1L); c.setGreutateBruta(BigDecimal.ZERO);
        asteaptaRegula("RB-15", c);
    }

    @Test void rb16_comandaFaraLinii() {
        ComandaTransport c = new ComandaTransport();
        c.setClientId(1L); c.setLocatieIncarcare(TestData.IASI); c.setLocatieDescarcare(TestData.MUNCHEN);
        c.setTarifAgreat(SumaMonetara.of("1000", Moneda.EUR));
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> validare.valideazaComanda(c));
        assertTrue(ex.hasCode("RB-16"));
    }

    @Test void rb17_scadentaInainteDeEmitere() {
        Factura f = new Factura();
        f.setClientId(1L); f.setMoneda(Moneda.EUR);
        f.setDataEmiterii(LocalDate.now()); f.setDataScadenta(LocalDate.now().minusDays(1));
        asteaptaRegula("RB-17", f);
    }
}
