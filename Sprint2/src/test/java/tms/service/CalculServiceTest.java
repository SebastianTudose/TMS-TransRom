package tms.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import tms.config.TmsConfig;
import tms.domain.entity.*;
import tms.domain.enums.Moneda;
import tms.domain.enums.StatusRuta;
import tms.domain.support.ComandaTransportFactory;
import tms.domain.vo.*;
import tms.service.calcul.*;
import tms.support.TestData;

/** Testează serviciile de calcul: câmpuri calculate (RB-42..45) și vederi calculate (RB-46..48). */
@SpringJUnitConfig(TmsConfig.class)
class CalculServiceTest {

    @Autowired CalculService calcul;
    @Autowired ComandaTransportFactory comandaFactory;

    private static void egal(String asteptat, BigDecimal real) {
        assertEquals(0, new BigDecimal(asteptat).compareTo(real), "Se aștepta " + asteptat + " dar s-a obținut " + real);
    }

    @Test void rb42_distantaParcursa() {
        FoaieParcurs f = new FoaieParcurs();
        f.setKmPlecare(new BigDecimal("120000")); f.setKmSosire(new BigDecimal("120350"));
        egal("350", calcul.calculeazaDistanta(f));
    }

    @Test void rb43_costTotalCuRotunjire() {
        SumaMonetara cost = calcul.calculeazaCostTotal(new BigDecimal("52.0"), SumaMonetara.of("7.85", Moneda.RON));
        egal("408.20", cost.valoare());
        assertEquals(Moneda.RON, cost.moneda());
        egal("100.00", calcul.calculeazaCostTotal(new BigDecimal("12.5"), SumaMonetara.of("8.00", Moneda.RON)).valoare());
    }

    @Test void rb44_greutateSiVolumComanda() {
        ComandaTransport c = comandaFactory.creeaza(5L, TestData.IASI, TestData.MUNCHEN, SumaMonetara.of("1000", Moneda.EUR), null,
                List.of(comandaFactory.creeazaLinie("Paleți", 10, new BigDecimal("120.5"), new BigDecimal("0.8")),
                        comandaFactory.creeazaLinie("Cutii", 5, new BigDecimal("40"), new BigDecimal("0.5"))));
        egal("1205", c.getLinii().get(0).getGreutateTotalaKg());
        egal("1405", calcul.calculeazaGreutateComanda(c));
        egal("10.5", calcul.calculeazaVolumComanda(c));
    }

    @Test void rb45_valoareFacturaCuDiscount() {
        LinieFactura l1 = new LinieFactura(); l1.setValoare(SumaMonetara.of("1000.00", Moneda.EUR));
        LinieFactura l2 = new LinieFactura(); l2.setValoare(SumaMonetara.of("500.00", Moneda.EUR));
        SumaMonetara total = calcul.calculeazaValoareFactura(List.of(l1, l2), new BigDecimal("5"), Moneda.EUR);
        egal("1425.00", total.valoare());
        egal("1500.00", calcul.calculeazaValoareFactura(List.of(l1, l2), BigDecimal.ZERO, Moneda.EUR).valoare());
    }

    @Test void rb46_consumMediuLa100Km() {
        Ruta r = new Ruta(); FoaieParcurs f = new FoaieParcurs(); f.setConsumCarburant(new BigDecimal("45.5"));
        r.setFoaieParcurs(f); r.setDistantaParcursa(new BigDecimal("350"));
        egal("13.00", calcul.calculeazaConsumMediu(r));
        r.setDistantaParcursa(BigDecimal.ZERO);
        egal("0", calcul.calculeazaConsumMediu(r));
    }

    @Test void rb47_gradIncarcare() {
        Ruta r = new Ruta(); AlocareComanda a = new AlocareComanda(); a.setGreutateKg(new BigDecimal("5000")); r.addAlocare(a);
        Camion camion = TestData.camion(10, "IS12ABC", "20000");
        egal("25.00", calcul.calculeazaGradIncarcare(r, camion));
    }

    private Ruta finalizata(long camionId, String cod, String km, String cost, LocalDateTime data) {
        Ruta r = new Ruta(); r.setCodRuta(cod); r.setCamionId(camionId); r.setStatus(StatusRuta.FINALIZATA);
        r.setDistantaParcursa(new BigDecimal(km));
        CostCombustibil c = new CostCombustibil(); c.setDataCalcul(data); c.setConsumTotal(BigDecimal.TEN);
        c.setPretPerLitru(SumaMonetara.of("8.00", Moneda.RON)); c.setCostTotal(SumaMonetara.of(cost, Moneda.RON));
        r.setCostCombustibil(c);
        return r;
    }

    @Test void rb48_centralizatorGrupatPeCamion() {
        LocalDateTime acum = LocalDateTime.now();
        Camion a = TestData.camion(10, "IS12ABC", "20000");
        Camion b = TestData.camion(11, "B123ABC", "20000");
        List<Ruta> rute = List.of(
                finalizata(10, "RT-2", "200", "100.00", acum.minusDays(1)),
                finalizata(10, "RT-1", "350", "408.20", acum.minusDays(3)),
                finalizata(11, "RT-3", "500", "700.00", acum.minusDays(2)),
                finalizata(11, "RT-4", "999", "999.00", acum.minusDays(40)));          // în afara perioadei
        CentralizatorConsum rez = calcul.centralizatorConsum(rute, Map.of(10L, a, 11L, b),
                new IntervalTimp(acum.minusDays(10), acum));
        assertEquals(2, rez.grupuri().size());
        assertEquals("B123ABC", rez.grupuri().get(0).nrInmatriculare());       // ordonat după număr
        assertEquals("RT-1", rez.grupuri().get(1).randuri().get(0).codRuta()); // cronologic în grup
        egal("550", rez.grupuri().get(1).distantaTotala());
        egal("508.20", rez.grupuri().get(1).costTotal());
        egal("1050", rez.distantaTotala());
        egal("1208.20", rez.costTotal());
        assertEquals(Moneda.RON, rez.moneda());
    }
}
