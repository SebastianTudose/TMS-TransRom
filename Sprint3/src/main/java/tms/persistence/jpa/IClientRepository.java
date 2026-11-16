package tms.persistence.jpa;

import java.util.Optional;
import tms.domain.entity.Client;

/** Repository „complet” pentru clienți: CRUD + căutarea după cod fiscal. */
public interface IClientRepository extends IRepository<Client, Long> {

    Optional<Client> findByCodFiscal(String codFiscal);
}
