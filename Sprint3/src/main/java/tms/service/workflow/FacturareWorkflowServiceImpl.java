package tms.service.workflow;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tms.domain.entity.Client;
import tms.domain.entity.ComandaTransport;
import tms.domain.entity.Factura;
import tms.domain.entity.LinieFactura;
import tms.domain.entity.Utilizator;
import tms.domain.enums.Rol;
import tms.domain.enums.StatusComanda;
import tms.domain.enums.StatusFactura;
import tms.domain.exception.BusinessRuleException;
import tms.domain.support.FacturaFactory;
import tms.persistence.listener.AuditContext;
import tms.persistence.repository.ClientRepository;
import tms.persistence.repository.ComandaTransportRepository;
import tms.persistence.repository.FacturaRepository;
import tms.service.audit.AuditService;
import tms.service.validation.ValidationService;

@Service
@Transactional
public class FacturareWorkflowServiceImpl implements FacturareWorkflowService {

    private final ValidationService validare;
    private final AuditService audit;
    private final FacturaFactory facturaFactory;
    private final FacturaRepository facturi;
    private final ClientRepository clienti;
    private final ComandaTransportRepository comenzi;

    public FacturareWorkflowServiceImpl(ValidationService validare, AuditService audit, FacturaFactory facturaFactory,
                                        FacturaRepository facturi, ClientRepository clienti, ComandaTransportRepository comenzi) {
        this.validare = validare; this.audit = audit; this.facturaFactory = facturaFactory;
        this.facturi = facturi; this.clienti = clienti; this.comenzi = comenzi;
    }

    private static String actor(Utilizator u) { return u.getEmail().valoare(); }

    private Client clientul(Long clientId) {
        return clienti.findById(clientId).orElseThrow(() -> new BusinessRuleException("RB-35", "Clientul facturii nu există"));
    }

    private List<ComandaTransport> comenzileFacturii(Factura factura) {
        return comenzi.findAllById(factura.getLinii().stream().map(LinieFactura::getComandaId).toList());
    }

    @Override
    public Factura emiteFactura(Utilizator u, Long clientId, Collection<Long> comandaIds, LocalDate dataScadenta) {
        validare.verificaAcces(u, Rol.CONTABIL);
        if (comandaIds.isEmpty() || new HashSet<>(comandaIds).size() != comandaIds.size())
            throw new BusinessRuleException("RB-35", "Factura necesită cel puțin o comandă, fără duplicate");
        Client client = clientul(clientId);
        List<ComandaTransport> lista = comenzi.findAllById(comandaIds);
        if (lista.size() != comandaIds.size())
            throw new BusinessRuleException("RB-35", "Una dintre comenzile indicate nu există");
        for (ComandaTransport c : lista) {                                                  // RB-35
            if (c.getStatus() != StatusComanda.LIVRATA)
                throw new BusinessRuleException("RB-35", "Comanda " + c.getNrComanda() + " nu este LIVRATA (poate fi deja facturată)");
            if (!client.getId().equals(c.getClientId()))
                throw new BusinessRuleException("RB-35", "Comanda " + c.getNrComanda() + " aparține altui client");
        }
        Factura factura = facturaFactory.creeaza(client, lista, dataScadenta);
        validare.valideazaFactura(factura, client);                                         // RB-17
        for (ComandaTransport c : lista) {                                                  // RB-36
            validare.verificaTranzitie("RB-36", c.getStatus(), StatusComanda.FACTURATA);
            c.setStatus(StatusComanda.FACTURATA);
        }
        client.setSoldDebitor(client.getSoldDebitor().add(factura.getValoareTotala().valoare()));
        Factura salvata = AuditContext.ca(actor(u), () -> facturi.save(factura));
        comenzi.saveAll(lista);
        clienti.save(client);
        lista.forEach(c -> audit.inregistreazaModificare(c, actor(u), "Status FACTURATA (" + salvata.getNumar() + ")"));
        audit.inregistreazaCreare(salvata, actor(u));
        audit.inregistreazaModificare(client, actor(u), "Sold crescut cu " + salvata.getValoareTotala().valoare());
        return salvata;
    }

    @Override
    public void incaseaza(Utilizator u, Factura factura) {
        validare.verificaAcces(u, Rol.CONTABIL);
        validare.verificaTranzitie("RB-37", factura.getStatus(), StatusFactura.INCASATA);
        Client client = clientul(factura.getClientId());
        factura.setStatus(StatusFactura.INCASATA);
        client.setSoldDebitor(client.getSoldDebitor().subtract(factura.getValoareTotala().valoare()));
        facturi.save(factura);
        clienti.save(client);
        audit.inregistreazaModificare(factura, actor(u), "Încasată");
        audit.inregistreazaModificare(client, actor(u), "Sold scăzut cu " + factura.getValoareTotala().valoare());
    }

    @Override
    public void storneaza(Utilizator u, Factura factura) {
        validare.verificaAcces(u, Rol.CONTABIL);
        validare.verificaTranzitie("RB-38", factura.getStatus(), StatusFactura.STORNATA);
        Client client = clientul(factura.getClientId());
        List<ComandaTransport> lista = comenzileFacturii(factura);
        for (ComandaTransport c : lista) validare.verificaTranzitie("RB-38", c.getStatus(), StatusComanda.LIVRATA);
        factura.setStatus(StatusFactura.STORNATA);                                          // nu se șterge (RB-33)
        client.setSoldDebitor(client.getSoldDebitor().subtract(factura.getValoareTotala().valoare()));
        for (ComandaTransport c : lista) c.setStatus(StatusComanda.LIVRATA);
        facturi.save(factura);
        clienti.save(client);
        comenzi.saveAll(lista);
        lista.forEach(c -> audit.inregistreazaModificare(c, actor(u), "Revine LIVRATA (storno " + factura.getNumar() + ")"));
        audit.inregistreazaModificare(factura, actor(u), "Stornată");
        audit.inregistreazaModificare(client, actor(u), "Sold scăzut cu " + factura.getValoareTotala().valoare());
    }
}
