package tms.service.workflow;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tms.domain.entity.ComandaTransport;
import tms.domain.entity.Utilizator;
import tms.domain.enums.Rol;
import tms.domain.enums.StatusComanda;
import tms.domain.exception.BusinessRuleException;
import tms.persistence.listener.AuditContext;
import tms.persistence.repository.ComandaTransportRepository;
import tms.service.audit.AuditService;
import tms.service.calcul.CalculService;
import tms.service.validation.ValidationService;

@Service
@Transactional
public class ComandaWorkflowServiceImpl implements ComandaWorkflowService {

    private final ValidationService validare;
    private final CalculService calcul;
    private final AuditService audit;
    private final ComandaTransportRepository comenzi;

    public ComandaWorkflowServiceImpl(ValidationService validare, CalculService calcul, AuditService audit,
                                      ComandaTransportRepository comenzi) {
        this.validare = validare; this.calcul = calcul; this.audit = audit; this.comenzi = comenzi;
    }

    @Override
    public ComandaTransport creeazaComanda(Utilizator u, ComandaTransport comanda) {
        validare.verificaAcces(u, Rol.DISPECER);
        comanda.getLinii().forEach(calcul::recalculeazaLinie);
        validare.valideazaComanda(comanda);
        comanda.setStatus(StatusComanda.NOUA);
        ComandaTransport salvata = AuditContext.ca(u.getEmail().valoare(), () -> comenzi.save(comanda));
        audit.inregistreazaCreare(salvata, u.getEmail().valoare());
        return salvata;
    }

    @Override
    public ComandaTransport modificaComanda(Utilizator u, ComandaTransport comanda) {
        validare.verificaAcces(u, Rol.DISPECER);
        if (comanda.getStatus() != StatusComanda.NOUA)
            throw new BusinessRuleException("RB-34", "O comandă poate fi modificată numai în starea NOUA");
        comanda.getLinii().forEach(calcul::recalculeazaLinie);
        validare.valideazaComanda(comanda);
        ComandaTransport salvata = comenzi.save(comanda);
        audit.inregistreazaModificare(salvata, u.getEmail().valoare(), "Modificare date comandă");
        return salvata;
    }

    @Override
    public ComandaTransport anuleazaComanda(Utilizator u, ComandaTransport comanda) {
        validare.verificaAcces(u, Rol.DISPECER);
        if (comanda.getStatus() != StatusComanda.NOUA)
            throw new BusinessRuleException("RB-34", "O comandă poate fi anulată numai în starea NOUA");
        comanda.setStatus(StatusComanda.ANULATA);
        ComandaTransport salvata = comenzi.save(comanda);
        audit.inregistreazaModificare(salvata, u.getEmail().valoare(), "Anulare comandă");
        return salvata;
    }
}
