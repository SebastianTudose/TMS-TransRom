package tms.domain.support;

import org.springframework.stereotype.Service;
import tms.domain.entity.Ruta;
import tms.domain.enums.StatusRuta;
import tms.domain.vo.IntervalTimp;
import tms.domain.vo.Locatie;

@Service
public class RutaFactoryImpl implements RutaFactory {

    private final SecventaCoduriService coduri;

    public RutaFactoryImpl(SecventaCoduriService coduri) { this.coduri = coduri; }

    @Override
    public Ruta creeaza(Long camionId, Long soferId, Locatie plecare, Locatie destinatie, IntervalTimp interval) {
        Ruta r = new Ruta();
        r.setCodRuta(coduri.urmatorulCod("RT"));
        r.setCamionId(camionId);
        r.setSoferId(soferId);
        r.setLocatiePlecare(plecare);
        r.setLocatieDestinatie(destinatie);
        r.setIntervalPlanificat(interval);
        r.setStatus(StatusRuta.PLANIFICATA);
        return r;
    }
}
