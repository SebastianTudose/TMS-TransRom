package tms.service.raportare;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tms.domain.entity.Camion;
import tms.domain.entity.Ruta;
import tms.domain.entity.Utilizator;
import tms.domain.enums.Rol;
import tms.domain.enums.StatusRuta;
import tms.domain.vo.IntervalTimp;
import tms.persistence.repository.CamionRepository;
import tms.persistence.repository.RutaRepository;
import tms.service.calcul.CalculService;
import tms.service.calcul.CentralizatorConsum;
import tms.service.validation.ValidationService;

@Service
@Transactional(readOnly = true)
public class RaportareServiceImpl implements RaportareService {

    private final ValidationService validare;
    private final CalculService calcul;
    private final RutaRepository rute;
    private final CamionRepository camioane;

    public RaportareServiceImpl(ValidationService validare, CalculService calcul, RutaRepository rute, CamionRepository camioane) {
        this.validare = validare; this.calcul = calcul; this.rute = rute; this.camioane = camioane;
    }

    @Override
    public List<Ruta> curseleMele(Utilizator sofer) {
        validare.verificaAcces(sofer, Rol.SOFER);
        return rute.findBySoferIdOrderByIntervalPlanificatInceputAsc(sofer.getId());
    }

    @Override
    public CentralizatorConsum centralizatorConsum(Utilizator utilizator, IntervalTimp perioada) {
        validare.verificaAcces(utilizator, Rol.ADMINISTRATOR, Rol.CONTABIL);
        List<Ruta> finalizate = rute.findCuCostInPerioada(StatusRuta.FINALIZATA, perioada.inceput(), perioada.sfarsit());
        Set<Long> idCamioane = finalizate.stream().map(Ruta::getCamionId).collect(Collectors.toSet());
        Map<Long, Camion> pePId = new HashMap<>();
        camioane.findAllById(idCamioane).forEach(c -> pePId.put(c.getId(), c));
        return calcul.centralizatorConsum(finalizate, pePId, perioada);
    }
}
