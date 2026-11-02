package tms.domain.support;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import tms.domain.entity.ComandaTransport;
import tms.domain.entity.LinieComanda;
import tms.domain.enums.StatusComanda;
import tms.domain.vo.Locatie;
import tms.domain.vo.SumaMonetara;
import tms.service.calcul.CalculService;

@Service
public class ComandaTransportFactoryImpl implements ComandaTransportFactory {

    private final SecventaCoduriService coduri;
    private final CalculService calcul;
    private final Clock clock;

    public ComandaTransportFactoryImpl(SecventaCoduriService coduri, CalculService calcul, Clock clock) {
        this.coduri = coduri; this.calcul = calcul; this.clock = clock;
    }

    @Override
    public ComandaTransport creeaza(Long clientId, Locatie incarcare, Locatie descarcare,
                                    SumaMonetara tarif, LocalDate dataLimitaLivrare, List<LinieComanda> linii) {
        ComandaTransport c = new ComandaTransport();
        c.setNrComanda(coduri.urmatorulCod("CMD"));
        c.setClientId(clientId);
        c.setDataComanda(LocalDate.now(clock));
        c.setDataLimitaLivrare(dataLimitaLivrare);
        c.setLocatieIncarcare(incarcare);
        c.setLocatieDescarcare(descarcare);
        c.setTarifAgreat(tarif);
        c.setStatus(StatusComanda.NOUA);
        if (linii != null) linii.forEach(c::addLinie);
        c.getLinii().forEach(calcul::recalculeazaLinie);      // câmpuri calculate (RB-44)
        return c;
    }

    @Override
    public LinieComanda creeazaLinie(String descriereMarfa, int cantitate, BigDecimal greutateUnitaraKg, BigDecimal volumUnitarM3) {
        LinieComanda l = new LinieComanda();
        l.setDescriereMarfa(descriereMarfa);
        l.setCantitate(cantitate);
        l.setGreutateUnitaraKg(greutateUnitaraKg);
        l.setVolumUnitarM3(volumUnitarM3);
        calcul.recalculeazaLinie(l);
        return l;
    }
}
