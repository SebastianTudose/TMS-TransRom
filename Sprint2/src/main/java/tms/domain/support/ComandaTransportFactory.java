package tms.domain.support;

import java.time.LocalDate;
import java.util.List;
import tms.domain.entity.ComandaTransport;
import tms.domain.entity.LinieComanda;
import tms.domain.vo.Locatie;
import tms.domain.vo.SumaMonetara;

/** Fabrică pentru agregatul ComandaTransport: asamblează rădăcina și componentele. */
public interface ComandaTransportFactory {
    ComandaTransport creeaza(Long clientId, Locatie incarcare, Locatie descarcare,
                             SumaMonetara tarif, LocalDate dataLimitaLivrare, List<LinieComanda> linii);

    LinieComanda creeazaLinie(String descriereMarfa, int cantitate,
                              java.math.BigDecimal greutateUnitaraKg, java.math.BigDecimal volumUnitarM3);
}
