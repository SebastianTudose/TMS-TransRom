package tms.domain.support;

import tms.domain.entity.Ruta;
import tms.domain.vo.IntervalTimp;
import tms.domain.vo.Locatie;

/** Fabrică pentru agregatul Ruta: o cursă nouă este PLANIFICATA și primește cod unic. */
public interface RutaFactory {
    Ruta creeaza(Long camionId, Long soferId, Locatie plecare, Locatie destinatie, IntervalTimp intervalPlanificat);
}
