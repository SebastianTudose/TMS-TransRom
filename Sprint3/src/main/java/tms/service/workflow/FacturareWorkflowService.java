package tms.service.workflow;

import java.time.LocalDate;
import java.util.Collection;
import tms.domain.entity.Factura;
import tms.domain.entity.Utilizator;

/** Servicii de workflow pentru facturare (UC-11, UC-12). Toate cer rolul CONTABIL; clientul și comenzile se încarcă din repository-uri. */
public interface FacturareWorkflowService {
    Factura emiteFactura(Utilizator u, Long clientId, Collection<Long> comandaIds, LocalDate dataScadenta);
    void incaseaza(Utilizator u, Factura factura);
    void storneaza(Utilizator u, Factura factura);
}
