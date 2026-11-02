package tms.domain.support;

import java.time.LocalDate;
import java.util.List;
import tms.domain.entity.Client;
import tms.domain.entity.ComandaTransport;
import tms.domain.entity.Factura;

/** Fabrică pentru agregatul Factura: o linie pentru fiecare comandă, total calculat cu discountul clientului. */
public interface FacturaFactory {
    Factura creeaza(Client client, List<ComandaTransport> comenzi, LocalDate dataScadenta);
}
