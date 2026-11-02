package tms.service.workflow;

import tms.domain.entity.ComandaTransport;
import tms.domain.entity.Utilizator;

/** Servicii de workflow (cazuri de utilizare) pentru comenzile de transport. */
public interface ComandaWorkflowService {
    ComandaTransport creeazaComanda(Utilizator utilizator, ComandaTransport comanda);   // UC-02
    void modificaComanda(Utilizator utilizator, ComandaTransport comanda);              // UC-02, RB-34
    void anuleazaComanda(Utilizator utilizator, ComandaTransport comanda);              // UC-02, RB-34
}
