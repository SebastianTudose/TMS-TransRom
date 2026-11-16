package tms.service.calcul;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import tms.domain.entity.*;
import tms.domain.enums.Moneda;
import tms.domain.vo.IntervalTimp;
import tms.domain.vo.SumaMonetara;

/** Servicii de calcul ale domeniului: câmpuri calculate (RB-42..45) și vederi calculate (RB-46..48). */
public interface CalculService {
    // ---- câmpuri calculate ----
    BigDecimal calculeazaDistanta(FoaieParcurs foaie);                                   // RB-42
    SumaMonetara calculeazaCostTotal(BigDecimal litri, SumaMonetara pretPerLitru);       // RB-43
    void recalculeazaLinie(LinieComanda linie);                                          // RB-44
    BigDecimal calculeazaGreutateComanda(ComandaTransport comanda);                      // RB-44
    BigDecimal calculeazaVolumComanda(ComandaTransport comanda);                         // RB-44
    SumaMonetara calculeazaValoareFactura(List<LinieFactura> linii, BigDecimal discountProcent, Moneda moneda); // RB-45
    BigDecimal calculeazaGreutateAlocata(Ruta ruta);

    // ---- vederi calculate ----
    BigDecimal calculeazaConsumMediu(Ruta ruta);                                         // RB-46 (l / 100 km)
    BigDecimal calculeazaGradIncarcare(Ruta ruta, Camion camion);                        // RB-47 (%)
    CentralizatorConsum centralizatorConsum(Collection<Ruta> rute, Map<Long, Camion> camioane, IntervalTimp perioada); // RB-48
}
