package tms.service.workflow;

import tms.domain.entity.ComandaTransport;
import tms.domain.entity.Utilizator;

/** Servicii de workflow pentru comenzi (UC-02). Toate cer rolul DISPECER; rezultatul este persistat. */
public interface ComandaWorkflowService {
    ComandaTransport creeazaComanda(Utilizator u, ComandaTransport comanda);
    ComandaTransport modificaComanda(Utilizator u, ComandaTransport comanda);
    ComandaTransport anuleazaComanda(Utilizator u, ComandaTransport comanda);
}
