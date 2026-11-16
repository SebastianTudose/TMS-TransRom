package tms.service.administrare;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tms.domain.entity.Utilizator;
import tms.domain.exception.BusinessRuleException;
import tms.domain.vo.AdresaEmail;
import tms.persistence.repository.UtilizatorRepository;

@Service
@Transactional(readOnly = true)
public class AutentificareServiceImpl implements AutentificareService {

    private final UtilizatorRepository utilizatori;

    public AutentificareServiceImpl(UtilizatorRepository utilizatori) { this.utilizatori = utilizatori; }

    @Override
    public Utilizator autentifica(String email, String hashParola) {
        Utilizator cont = utilizatori.findByEmail(new AdresaEmail(email))
                .filter(u -> u.getHashParola() != null && u.getHashParola().equals(hashParola))
                .orElseThrow(() -> new BusinessRuleException("RB-39", "Credențiale invalide"));
        if (!cont.isActiv())
            throw new BusinessRuleException("RB-40", "Contul este dezactivat și nu poate fi folosit pentru autentificare");
        return cont;
    }
}
