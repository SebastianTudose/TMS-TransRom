package tms.domain.support;

import org.springframework.stereotype.Service;
import tms.domain.entity.Ruta;
import tms.domain.enums.StatusRuta;
import tms.domain.exception.BusinessRuleException;
import tms.domain.vo.IntervalTimp;
import tms.domain.vo.Locatie;
import tms.persistence.repository.CamionRepository;
import tms.persistence.repository.SoferRepository;

@Service
public class RutaFactoryImpl implements RutaFactory {

    private final SecventaCoduriService coduri;
    private final CamionRepository camioane;
    private final SoferRepository soferi;

    public RutaFactoryImpl(SecventaCoduriService coduri, CamionRepository camioane, SoferRepository soferi) {
        this.coduri = coduri; this.camioane = camioane; this.soferi = soferi;
    }

    @Override
    public Ruta creeaza(Long camionId, Long soferId, Locatie plecare, Locatie destinatie, IntervalTimp interval) {
        if (camionId != null && !camioane.existsById(camionId))
            throw new BusinessRuleException("RB-18", "Camionul cursei nu există");
        if (soferId != null && !soferi.existsById(soferId))
            throw new BusinessRuleException("RB-18", "Șoferul cursei nu există");
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
