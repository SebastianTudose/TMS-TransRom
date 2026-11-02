package tms.service.workflow;

import java.math.BigDecimal;
import java.util.Collection;
import tms.domain.entity.*;
import tms.domain.vo.SumaMonetara;

/** Servicii de workflow pentru ciclul de viață al unei curse (UC-03 … UC-09). Toate cer rolul DISPECER. */
public interface RutaWorkflowService {
    Ruta planificaRuta(Utilizator u, Ruta ruta, Camion camion, Sofer sofer, Collection<Ruta> rutePeResurse);    // UC-03
    Cmr alocaComanda(Utilizator u, Ruta ruta, ComandaTransport comanda, Camion camion);                         // UC-04, UC-05
    void dealocaComanda(Utilizator u, Ruta ruta, ComandaTransport comanda, Cmr cmr);                            // UC-05
    void pornesteRuta(Utilizator u, Ruta ruta, Camion camion, Collection<ComandaTransport> comenzi, BigDecimal kmPlecare); // UC-06
    void finalizeazaRuta(Utilizator u, Ruta ruta, Camion camion, Collection<ComandaTransport> comenzi,
                         BigDecimal kmSosire, BigDecimal consumLitri, SumaMonetara pretPerLitru);               // UC-07
    void corecteazaFinalizare(Utilizator u, Ruta ruta, Collection<ComandaTransport> comenzi,
                              BigDecimal kmSosire, BigDecimal consumLitri, SumaMonetara pretPerLitru);          // UC-08
    void anuleazaFinalizarea(Utilizator u, Ruta ruta, Camion camion, Collection<ComandaTransport> comenzi);     // UC-08
    void anuleazaRuta(Utilizator u, Ruta ruta, Camion camion, Collection<ComandaTransport> comenzi, Collection<Cmr> cmrUri); // UC-09
}
