package tms.service.administrare;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tms.domain.entity.Camion;
import tms.domain.entity.Client;
import tms.domain.entity.Utilizator;
import tms.domain.enums.Rol;
import tms.domain.enums.StatusCamion;
import tms.domain.exception.BusinessRuleException;
import tms.persistence.listener.AuditContext;
import tms.persistence.repository.CamionRepository;
import tms.persistence.repository.ClientRepository;
import tms.persistence.repository.UtilizatorRepository;
import tms.service.audit.AuditService;
import tms.service.validation.ValidationService;

@Service
@Transactional
public class AdministrareServiceImpl implements AdministrareService {

    private final ValidationService validare;
    private final AuditService audit;
    private final UtilizatorRepository utilizatori;
    private final ClientRepository clienti;
    private final CamionRepository camioane;

    public AdministrareServiceImpl(ValidationService validare, AuditService audit, UtilizatorRepository utilizatori,
                                   ClientRepository clienti, CamionRepository camioane) {
        this.validare = validare; this.audit = audit;
        this.utilizatori = utilizatori; this.clienti = clienti; this.camioane = camioane;
    }

    private static String actor(Utilizator u) { return u.getEmail().valoare(); }

    // ---------------------------------------------------------------- UC-13
    @Override
    public Utilizator creeazaCont(Utilizator admin, Utilizator cont) {
        validare.verificaAcces(admin, Rol.ADMINISTRATOR);
        validare.valideazaEntitate(cont);
        if (utilizatori.existsByEmail(cont.getEmail()))
            throw new BusinessRuleException("RB-01", "Adresa de email există deja în sistem");
        Utilizator salvat = AuditContext.ca(actor(admin), () -> utilizatori.save(cont));
        audit.inregistreazaCreare(salvat, actor(admin));
        return salvat;
    }

    private Utilizator contul(Long id) {
        return utilizatori.findById(id).orElseThrow(() -> new BusinessRuleException("RB-39", "Contul nu există"));
    }

    @Override
    public void dezactiveazaCont(Utilizator admin, Long contId) {
        validare.verificaAcces(admin, Rol.ADMINISTRATOR);
        Utilizator cont = contul(contId);
        cont.setActiv(false);                              // RB-40: contul se dezactivează, nu se șterge
        utilizatori.save(cont);
        audit.inregistreazaModificare(cont, actor(admin), "Cont dezactivat");
    }

    @Override
    public void reactiveazaCont(Utilizator admin, Long contId) {
        validare.verificaAcces(admin, Rol.ADMINISTRATOR);
        Utilizator cont = contul(contId);
        cont.setActiv(true);
        utilizatori.save(cont);
        audit.inregistreazaModificare(cont, actor(admin), "Cont reactivat");
    }

    // ---------------------------------------------------------------- UC-14
    @Override
    public Client creeazaClient(Utilizator admin, Client client) {
        validare.verificaAcces(admin, Rol.ADMINISTRATOR);
        validare.valideazaEntitate(client);
        if (clienti.existsByCodFiscal(client.getCodFiscal()))
            throw new BusinessRuleException("RB-03", "Codul fiscal există deja în sistem");
        Client salvat = AuditContext.ca(actor(admin), () -> clienti.save(client));
        audit.inregistreazaCreare(salvat, actor(admin));
        return salvat;
    }

    @Override
    public Client modificaClient(Utilizator admin, Client client) {
        validare.verificaAcces(admin, Rol.ADMINISTRATOR);
        validare.valideazaEntitate(client);
        clienti.findByCodFiscal(client.getCodFiscal()).ifPresent(existent -> {
            if (!existent.getId().equals(client.getId()))
                throw new BusinessRuleException("RB-03", "Codul fiscal aparține altui client");
        });
        Client salvat = clienti.save(client);
        audit.inregistreazaModificare(salvat, actor(admin), "Modificare date client");
        return salvat;
    }

    // ---------------------------------------------------------------- UC-15
    @Override
    public Camion inregistreazaCamion(Utilizator admin, Camion camion) {
        validare.verificaAcces(admin, Rol.ADMINISTRATOR);
        validare.valideazaEntitate(camion);
        if (camioane.existsByNrInmatriculare(camion.getNrInmatriculare()))
            throw new BusinessRuleException("RB-06", "Numărul de înmatriculare există deja în sistem");
        Camion salvat = AuditContext.ca(actor(admin), () -> camioane.save(camion));
        audit.inregistreazaCreare(salvat, actor(admin));
        return salvat;
    }

    private Camion camionul(Long id) {
        return camioane.findById(id).orElseThrow(() -> new BusinessRuleException("RB-21", "Camionul nu există"));
    }

    @Override
    public void trimiteInService(Utilizator admin, Long camionId) {
        validare.verificaAcces(admin, Rol.ADMINISTRATOR);
        Camion camion = camionul(camionId);
        validare.verificaTranzitie("RB-21", camion.getStatusTehnic(), StatusCamion.IN_SERVICE);
        camion.setStatusTehnic(StatusCamion.IN_SERVICE);
        camioane.save(camion);
        audit.inregistreazaModificare(camion, actor(admin), "Status IN_SERVICE");
    }

    @Override
    public void revineDinService(Utilizator admin, Long camionId) {
        validare.verificaAcces(admin, Rol.ADMINISTRATOR);
        Camion camion = camionul(camionId);
        validare.verificaTranzitie("RB-21", camion.getStatusTehnic(), StatusCamion.DISPONIBIL);
        camion.setStatusTehnic(StatusCamion.DISPONIBIL);
        camioane.save(camion);
        audit.inregistreazaModificare(camion, actor(admin), "Status DISPONIBIL");
    }
}
