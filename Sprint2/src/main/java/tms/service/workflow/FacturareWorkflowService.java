package tms.service.workflow;

import java.time.LocalDate;
import java.util.List;
import tms.domain.entity.*;

/** Servicii de workflow pentru facturare (UC-11, UC-12). Toate cer rolul CONTABIL. */
public interface FacturareWorkflowService {
    Factura emiteFactura(Utilizator u, Client client, List<ComandaTransport> comenzi, LocalDate dataScadenta);  // UC-11
    void incaseaza(Utilizator u, Client client, Factura factura);                                               // UC-12
    void storneaza(Utilizator u, Client client, Factura factura, List<ComandaTransport> comenzi);               // UC-12
}
