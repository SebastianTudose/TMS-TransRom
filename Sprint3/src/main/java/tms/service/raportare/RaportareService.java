package tms.service.raportare;

import java.util.List;
import tms.domain.entity.Ruta;
import tms.domain.entity.Utilizator;
import tms.domain.vo.IntervalTimp;
import tms.service.calcul.CentralizatorConsum;

/** Interogări de consultare (UC-10, UC-16): operații de citire peste repository-uri. */
public interface RaportareService {

    /** Cursele șoferului autentificat, în ordinea plecării (UC-10, rol SOFER). */
    List<Ruta> curseleMele(Utilizator sofer);

    /** Centralizatorul de consum și costuri auto pe o perioadă (UC-16, roluri ADMINISTRATOR sau CONTABIL, RB-48). */
    CentralizatorConsum centralizatorConsum(Utilizator utilizator, IntervalTimp perioada);
}
