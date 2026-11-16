package tms.persistence.jpa;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tms.domain.entity.Client;

/** Implementarea JPA a {@link IClientRepository}, cu {@code EntityManager} injectat. */
@Repository
public class ClientRepositoryJPA extends AbstractRepositoryJPA<Client, Long> implements IClientRepository {

    public ClientRepositoryJPA() { super(Client.class); }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> findByCodFiscal(String codFiscal) {
        return em.createQuery("select c from Client c where c.codFiscal = :cod", Client.class)
                .setParameter("cod", codFiscal)
                .getResultStream().findFirst();
    }
}
