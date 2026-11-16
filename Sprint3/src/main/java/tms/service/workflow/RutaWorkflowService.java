package tms.service.workflow;

import java.math.BigDecimal;
import tms.domain.entity.Camion;
import tms.domain.entity.Cmr;
import tms.domain.entity.ComandaTransport;
import tms.domain.entity.Ruta;
import tms.domain.entity.Sofer;
import tms.domain.entity.Utilizator;
import tms.domain.vo.SumaMonetara;

/**
 * Servicii de workflow pentru ciclul de viață al unei curse (UC-03 … UC-09). Toate cer rolul DISPECER.
 * Camionul, comenzile alocate și CMR-urile unei curse sunt încărcate din repository-uri, nu primite ca parametri.
 */
public interface RutaWorkflowService {
    Ruta planificaRuta(Utilizator u, Ruta ruta, Camion camion, Sofer sofer);                                    // UC-03
    Cmr alocaComanda(Utilizator u, Ruta ruta, ComandaTransport comanda);                                        // UC-04, UC-05
    void dealocaComanda(Utilizator u, Ruta ruta, ComandaTransport comanda);                                     // UC-05
    void pornesteRuta(Utilizator u, Ruta ruta, BigDecimal kmPlecare);                                           // UC-06
    void finalizeazaRuta(Utilizator u, Ruta ruta, BigDecimal kmSosire, BigDecimal consumLitri,
                         SumaMonetara pretPerLitru);                                                            // UC-07
    void corecteazaFinalizare(Utilizator u, Ruta ruta, BigDecimal kmSosire, BigDecimal consumLitri,
                              SumaMonetara pretPerLitru);                                                       // UC-08
    void anuleazaFinalizarea(Utilizator u, Ruta ruta);                                                          // UC-08
    void anuleazaRuta(Utilizator u, Ruta ruta);                                                                 // UC-09
}
