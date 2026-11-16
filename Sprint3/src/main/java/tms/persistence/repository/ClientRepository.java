package tms.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tms.domain.entity.Client;
import tms.domain.enums.Moneda;

/** Repository pentru agregatul Client. */
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByCodFiscal(String codFiscal);

    boolean existsByCodFiscal(String codFiscal);

    List<Client> findByMonedaFacturare(Moneda moneda);

    List<Client> findByDenumireContainingIgnoreCase(String fragment);

    /** Clienții cu sold debitor pozitiv, cei mai mari datornici primii. */
    @Query("select c from Client c where c.soldDebitor > 0 order by c.soldDebitor desc")
    List<Client> findCuSoldDebitor();
}
