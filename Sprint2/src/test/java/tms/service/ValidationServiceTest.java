package tms.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
import tms.service.validation.ValidationService;
import tms.support.TestData;

/** Testează serviciile de validare a regulilor care implică mai multe obiecte (RB-01, 03, 19, 21-25, 28, 39). */
@SpringJUnitConfig(TmsConfig.class)
class ValidationServiceTest {

    @Autowired ValidationService validare;
    @Autowired RutaFactory rutaFactory;
    @Autowired ComandaTransportFactory comandaFactory;

    private Ruta ruta(long id, Camion camion, Sofer sofer, int dupaZile, int durataZile) {
        LocalDateTime start = LocalDateTime.now().plusDays(dupaZile);
        Ruta r = rutaFactory.creeaza(camion.getId(), sofer.getId(), TestData.IASI, TestData.MUNCHEN,
                new IntervalTimp(start, start.plusDays(durataZile)));
        r.setId(id);
        return r;
    }

    private void asteapta(String cod, Runnable actiune) {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, actiune::run);
        assertTrue(ex.hasCode(cod), "Se aștepta " + cod + " dar s-a primit: " + ex.getMessage());
    }

    @Test void rb39_rolulGresitEsteRefuzat() {
        asteapta("RB-39", () -> validare.verificaAcces(TestData.contabil(), Rol.DISPECER));
        assertDoesNotThrow(() -> validare.verificaAcces(TestData.dispecer(), Rol.DISPECER));
    }

    @Test void rb39_contulDezactivatEsteRefuzat() {
        Dispecer d = TestData.dispecer(); d.setActiv(false);
        asteapta("RB-39", () -> validare.verificaAcces(d, Rol.DISPECER));
    }

    @Test void rb01_emailDuplicat() {
        asteapta("RB-01", () -> validare.verificaEmailUnic(new AdresaEmail("DISPECER@tms.ro"), List.of(TestData.dispecer())));
        assertDoesNotThrow(() -> validare.verificaEmailUnic(new AdresaEmail("altul@tms.ro"), List.of(TestData.dispecer())));
    }

    @Test void rb03_codFiscalDuplicat() {
        Client existent = TestData.client(1, "0", Moneda.EUR);
        Client nou = TestData.client(2, "0", Moneda.EUR);
        asteapta("RB-03", () -> validare.verificaCodFiscalUnic(nou, List.of(existent)));
    }

    @Test void tranzitiaDeStareNepermisaEsteRefuzata() {
        asteapta("RB-X", () -> validare.verificaTranzitie("RB-X", StatusComanda.LIVRATA, StatusComanda.NOUA));
        assertDoesNotThrow(() -> validare.verificaTranzitie("RB-X", StatusComanda.NOUA, StatusComanda.PLANIFICATA));
    }

    @Test void rb21_camionInServiceNuPoateFiAlocat() {
        Camion camion = TestData.camion(10, "IS12ABC", "20000"); camion.setStatusTehnic(StatusCamion.IN_SERVICE);
        Sofer sofer = TestData.sofer(20, LocalDate.now().plusYears(2));
        asteapta("RB-21", () -> validare.valideazaPlanificare(ruta(1, camion, sofer, 1, 2), camion, sofer, List.of()));
    }

    @Test void rb22_suprapunereCamionSauSofer() {
        Camion camion = TestData.camion(10, "IS12ABC", "20000");
        Sofer sofer = TestData.sofer(20, LocalDate.now().plusYears(2));
        Ruta existenta = ruta(1, camion, sofer, 1, 3);
        Ruta noua = ruta(2, camion, sofer, 2, 2);                       // se suprapune cu prima
        asteapta("RB-22", () -> validare.valideazaPlanificare(noua, camion, sofer, List.of(existenta)));
        Ruta dupa = ruta(3, camion, sofer, 5, 2);                       // după prima: permis
        assertDoesNotThrow(() -> validare.valideazaPlanificare(dupa, camion, sofer, List.of(existenta)));
    }

    @Test void rb22_cursaAnulataNuBlocheazaResursele() {
        Camion camion = TestData.camion(10, "IS12ABC", "20000");
        Sofer sofer = TestData.sofer(20, LocalDate.now().plusYears(2));
        Ruta anulata = ruta(1, camion, sofer, 1, 3); anulata.setStatus(StatusRuta.ANULATA);
        Ruta noua = ruta(2, camion, sofer, 2, 2);
        assertDoesNotThrow(() -> validare.valideazaPlanificare(noua, camion, sofer, List.of(anulata)));
    }

    @Test void rb23_permisExpiratInainteDeSosire() {
        Camion camion = TestData.camion(10, "IS12ABC", "20000");
        Sofer sofer = TestData.sofer(20, LocalDate.now().plusDays(1));
        asteapta("RB-23", () -> validare.valideazaPlanificare(ruta(1, camion, sofer, 1, 5), camion, sofer, List.of()));
    }

    @Test void rb24_plecareInTrecut() {
        Camion camion = TestData.camion(10, "IS12ABC", "20000");
        Sofer sofer = TestData.sofer(20, LocalDate.now().plusYears(2));
        asteapta("RB-24", () -> validare.valideazaPlanificare(ruta(1, camion, sofer, -2, 1), camion, sofer, List.of()));
    }

    @Test void rb19_depasireCapacitate() {
        Camion camion = TestData.camion(10, "IS12ABC", "20000");
        Sofer sofer = TestData.sofer(20, LocalDate.now().plusYears(2));
        Ruta r = ruta(1, camion, sofer, 1, 2);
        ComandaTransport c = TestData.comanda(comandaFactory, 100, 5, "1000", Moneda.EUR, 10, "100");
        asteapta("RB-19", () -> validare.valideazaAlocare(r, c, camion, new BigDecimal("20001")));
        assertDoesNotThrow(() -> validare.valideazaAlocare(r, c, camion, new BigDecimal("20000")));
    }

    @Test void rb25_comandaNenouaNuSePoateAloca() {
        Camion camion = TestData.camion(10, "IS12ABC", "20000");
        Sofer sofer = TestData.sofer(20, LocalDate.now().plusYears(2));
        Ruta r = ruta(1, camion, sofer, 1, 2);
        ComandaTransport c = TestData.comanda(comandaFactory, 100, 5, "1000", Moneda.EUR, 10, "100");
        c.setStatus(StatusComanda.LIVRATA);
        asteapta("RB-25", () -> validare.valideazaAlocare(r, c, camion, new BigDecimal("1000")));
    }

    @Test void rb28_pornireFaraComenzi() {
        Camion camion = TestData.camion(10, "IS12ABC", "20000");
        Sofer sofer = TestData.sofer(20, LocalDate.now().plusYears(2));
        Ruta r = ruta(1, camion, sofer, 1, 2);
        asteapta("RB-28", () -> validare.valideazaPornire(r, camion, List.of(), new BigDecimal("1000")));
    }
}
