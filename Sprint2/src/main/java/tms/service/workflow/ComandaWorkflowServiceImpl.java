package tms.service.workflow;

import org.springframework.stereotype.Service;
import tms.domain.entity.ComandaTransport;
import tms.domain.entity.Utilizator;
import tms.domain.enums.Rol;
import tms.domain.enums.StatusComanda;
import tms.domain.exception.BusinessRuleException;
import tms.service.audit.AuditService;
import tms.service.calcul.CalculService;
import tms.service.validation.ValidationService;

@Service
public class ComandaWorkflowServiceImpl implements ComandaWorkflowService {

    private final ValidationService validare;
    private final CalculService calcul;
    private final AuditService audit;

    public ComandaWorkflowServiceImpl(ValidationService validare, CalculService calcul, AuditService audit) {
        this.validare = validare; this.calcul = calcul; this.audit = audit;
    }

    @Override
    public ComandaTransport creeazaComanda(Utilizator u, ComandaTransport comanda) {
        validare.verificaAcces(u, Rol.DISPECER);
        comanda.getLinii().forEach(calcul::recalculeazaLinie);
        validare.valideazaComanda(comanda);
        comanda.setStatus(StatusComanda.NOUA);
        audit.inregistreazaCreare(comanda, u.getEmail().valoare());
        return comanda;
    }

    @Override
    public void modificaComanda(Utilizator u, ComandaTransport comanda) {
        validare.verificaAcces(u, Rol.DISPECER);
        if (comanda.getStatus() != StatusComanda.NOUA)
            throw new BusinessRuleException("RB-34", "O comandă poate fi modificată numai în starea NOUA");
        comanda.getLinii().forEach(calcul::recalculeazaLinie);
        validare.valideazaComanda(comanda);
        audit.inregistreazaModificare(comanda, u.getEmail().valoare(), "Modificare date comandă");
    }

    @Override
    public void anuleazaComanda(Utilizator u, ComandaTransport comanda) {
        validare.verificaAcces(u, Rol.DISPECER);
        if (comanda.getStatus() != StatusComanda.NOUA)
            throw new BusinessRuleException("RB-34", "O comandă poate fi anulată numai în starea NOUA");
        comanda.setStatus(StatusComanda.ANULATA);
        audit.inregistreazaModificare(comanda, u.getEmail().valoare(), "Anulare comandă");
    }
}
