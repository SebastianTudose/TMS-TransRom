package tms.service.workflow;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import tms.domain.entity.*;
import tms.domain.enums.Rol;
import tms.domain.enums.StatusComanda;
import tms.domain.enums.StatusFactura;
import tms.domain.exception.BusinessRuleException;
import tms.domain.support.FacturaFactory;
import tms.service.audit.AuditService;
import tms.service.validation.ValidationService;

@Service
public class FacturareWorkflowServiceImpl implements FacturareWorkflowService {

    private final ValidationService validare;
    private final AuditService audit;
    private final FacturaFactory facturaFactory;

    public FacturareWorkflowServiceImpl(ValidationService validare, AuditService audit, FacturaFactory facturaFactory) {
        this.validare = validare; this.audit = audit; this.facturaFactory = facturaFactory;
    }

    private static String actor(Utilizator u) { return u.getEmail().valoare(); }

    @Override
    public Factura emiteFactura(Utilizator u, Client client, List<ComandaTransport> comenzi, LocalDate dataScadenta) {
        validare.verificaAcces(u, Rol.CONTABIL);
        if (comenzi.isEmpty() || new HashSet<>(comenzi).size() != comenzi.size())
            throw new BusinessRuleException("RB-35", "Factura necesită cel puțin o comandă, fără duplicate");
        for (ComandaTransport c : comenzi) {                                                // RB-35
            if (c.getStatus() != StatusComanda.LIVRATA)
                throw new BusinessRuleException("RB-35", "Comanda " + c.getNrComanda() + " nu este LIVRATA (poate fi deja facturată)");
            if (!client.getId().equals(c.getClientId()))
                throw new BusinessRuleException("RB-35", "Comanda " + c.getNrComanda() + " aparține altui client");
        }
        Factura factura = facturaFactory.creeaza(client, comenzi, dataScadenta);
        validare.valideazaFactura(factura, client);                                         // RB-17

        for (ComandaTransport c : comenzi) {                                                // RB-36
            validare.verificaTranzitie("RB-36", c.getStatus(), StatusComanda.FACTURATA);
            c.setStatus(StatusComanda.FACTURATA);
            audit.inregistreazaModificare(c, actor(u), "Status FACTURATA (" + factura.getNumar() + ")");
        }
        client.setSoldDebitor(client.getSoldDebitor().add(factura.getValoareTotala().valoare()));
        audit.inregistreazaCreare(factura, actor(u));
        audit.inregistreazaModificare(client, actor(u), "Sold crescut cu " + factura.getValoareTotala().valoare());
        return factura;
    }

    @Override
    public void incaseaza(Utilizator u, Client client, Factura factura) {
        validare.verificaAcces(u, Rol.CONTABIL);
        validare.verificaTranzitie("RB-37", factura.getStatus(), StatusFactura.INCASATA);
        factura.setStatus(StatusFactura.INCASATA);
        client.setSoldDebitor(client.getSoldDebitor().subtract(factura.getValoareTotala().valoare()));
        audit.inregistreazaModificare(factura, actor(u), "Încasată");
        audit.inregistreazaModificare(client, actor(u), "Sold scăzut cu " + factura.getValoareTotala().valoare());
    }

    @Override
    public void storneaza(Utilizator u, Client client, Factura factura, List<ComandaTransport> comenzi) {
        validare.verificaAcces(u, Rol.CONTABIL);
        validare.verificaTranzitie("RB-38", factura.getStatus(), StatusFactura.STORNATA);
        factura.setStatus(StatusFactura.STORNATA);                                          // nu se șterge (RB-33)
        client.setSoldDebitor(client.getSoldDebitor().subtract(factura.getValoareTotala().valoare()));
        for (ComandaTransport c : comenzi) {
            validare.verificaTranzitie("RB-38", c.getStatus(), StatusComanda.LIVRATA);
            c.setStatus(StatusComanda.LIVRATA);
            audit.inregistreazaModificare(c, actor(u), "Revine LIVRATA (storno " + factura.getNumar() + ")");
        }
        audit.inregistreazaModificare(factura, actor(u), "Stornată");
        audit.inregistreazaModificare(client, actor(u), "Sold scăzut cu " + factura.getValoareTotala().valoare());
    }
}
