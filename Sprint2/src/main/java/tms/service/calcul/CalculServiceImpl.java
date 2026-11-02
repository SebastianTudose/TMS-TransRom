package tms.service.calcul;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import tms.domain.entity.*;
import tms.domain.enums.Moneda;
import tms.domain.enums.StatusRuta;
import tms.domain.exception.BusinessRuleException;
import tms.domain.vo.IntervalTimp;
import tms.domain.vo.SumaMonetara;

@Service
public class CalculServiceImpl implements CalculService {

    private static final BigDecimal SUTA = new BigDecimal("100");

    @Override
    public BigDecimal calculeazaDistanta(FoaieParcurs f) {
        return f.getKmSosire().subtract(f.getKmPlecare());
    }

    @Override
    public SumaMonetara calculeazaCostTotal(BigDecimal litri, SumaMonetara pret) {
        return pret.inmultit(litri);
    }

    @Override
    public void recalculeazaLinie(LinieComanda l) {
        if (l.getCantitate() == null || l.getGreutateUnitaraKg() == null || l.getVolumUnitarM3() == null) return;
        BigDecimal q = BigDecimal.valueOf(l.getCantitate());
        l.setGreutateTotalaKg(l.getGreutateUnitaraKg().multiply(q));
        l.setVolumTotalM3(l.getVolumUnitarM3().multiply(q));
    }

    @Override
    public BigDecimal calculeazaGreutateComanda(ComandaTransport c) {
        return c.getLinii().stream().map(LinieComanda::getGreutateTotalaKg).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculeazaVolumComanda(ComandaTransport c) {
        return c.getLinii().stream().map(LinieComanda::getVolumTotalM3).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public SumaMonetara calculeazaValoareFactura(List<LinieFactura> linii, BigDecimal discount, Moneda moneda) {
        BigDecimal suma = linii.stream().map(l -> l.getValoare().valoare()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal factor = BigDecimal.ONE.subtract(discount.divide(SUTA));
        return new SumaMonetara(suma.multiply(factor).setScale(2, RoundingMode.HALF_UP), moneda);
    }

    @Override
    public BigDecimal calculeazaGreutateAlocata(Ruta r) {
        return r.getAlocari().stream().map(AlocareComanda::getGreutateKg).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculeazaConsumMediu(Ruta r) {
        FoaieParcurs f = r.getFoaieParcurs();
        if (f == null || f.getConsumCarburant() == null || r.getDistantaParcursa().signum() == 0) return BigDecimal.ZERO;
        return f.getConsumCarburant().multiply(SUTA).divide(r.getDistantaParcursa(), 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculeazaGradIncarcare(Ruta r, Camion c) {
        return calculeazaGreutateAlocata(r).multiply(SUTA).divide(c.getCapacitateKg(), 2, RoundingMode.HALF_UP);
    }

    @Override
    public CentralizatorConsum centralizatorConsum(Collection<Ruta> rute, Map<Long, Camion> camioane, IntervalTimp perioada) {
        Map<String, List<Ruta>> pePCamion = new TreeMap<>();
        for (Ruta r : rute) {
            CostCombustibil cost = r.getCostCombustibil();
            if (r.getStatus() != StatusRuta.FINALIZATA || cost == null) continue;
            LocalDateTime dc = cost.getDataCalcul();
            if (dc.isBefore(perioada.inceput()) || dc.isAfter(perioada.sfarsit())) continue;
            String nr = camioane.get(r.getCamionId()).getNrInmatriculare().valoare();
            pePCamion.computeIfAbsent(nr, k -> new ArrayList<>()).add(r);
        }
        List<GrupCamion> grupuri = new ArrayList<>();
        BigDecimal distTot = BigDecimal.ZERO, costTot = BigDecimal.ZERO;
        Moneda moneda = null;
        for (Map.Entry<String, List<Ruta>> e : pePCamion.entrySet()) {
            e.getValue().sort(Comparator.comparing(r -> r.getCostCombustibil().getDataCalcul()));
            List<RandCentralizator> randuri = new ArrayList<>();
            BigDecimal dist = BigDecimal.ZERO, cst = BigDecimal.ZERO;
            for (Ruta r : e.getValue()) {
                CostCombustibil c = r.getCostCombustibil();
                Moneda m = c.getCostTotal().moneda();
                if (moneda == null) moneda = m;
                else if (moneda != m) throw new BusinessRuleException("RB-48", "Costurile din raport sunt în monede diferite");
                randuri.add(new RandCentralizator(r.getCodRuta(), r.getDistantaParcursa(), c.getConsumTotal(),
                        c.getPretPerLitru(), c.getCostTotal(), c.getDataCalcul()));
                dist = dist.add(r.getDistantaParcursa());
                cst = cst.add(c.getCostTotal().valoare());
            }
            grupuri.add(new GrupCamion(e.getKey(), randuri, dist, cst));
            distTot = distTot.add(dist);
            costTot = costTot.add(cst);
        }
        return new CentralizatorConsum(grupuri, distTot, costTot, moneda);
    }
}
