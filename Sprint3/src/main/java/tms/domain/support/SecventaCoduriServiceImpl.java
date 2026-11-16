package tms.domain.support;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tms.persistence.entity.SecventaCod;
import tms.persistence.repository.SecventaCodRepository;

@Service
public class SecventaCoduriServiceImpl implements SecventaCoduriService {

    private final SecventaCodRepository secvente;
    private final Clock clock;

    public SecventaCoduriServiceImpl(SecventaCodRepository secvente, Clock clock) {
        this.secvente = secvente;
        this.clock = clock;
    }

    @Override
    @Transactional
    public String urmatorulCod(String prefix) {
        String cheie = prefix + "-" + LocalDate.now(clock).getYear();
        SecventaCod secventa = secvente.findByCheie(cheie).orElseGet(() -> secvente.save(new SecventaCod(cheie)));
        return String.format("%s-%04d", cheie, secventa.urmatoareaValoare());
    }
}
