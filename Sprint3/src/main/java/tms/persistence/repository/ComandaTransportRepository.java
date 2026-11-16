package tms.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tms.domain.entity.ComandaTransport;
import tms.domain.enums.StatusComanda;

/** Repository pentru rădăcina agregatului ComandaTransport (liniile se salvează prin cascadă). */
public interface ComandaTransportRepository extends JpaRepository<ComandaTransport, Long> {

    Optional<ComandaTransport> findByNrComanda(String nrComanda);

    List<ComandaTransport> findByStatus(StatusComanda status);

    List<ComandaTransport> findByClientIdAndStatus(Long clientId, StatusComanda status);

    /** Comenzile cu termenul de livrare depășit și care încă nu au fost livrate/facturate/anulate. */
    List<ComandaTransport> findByStatusAndDataLimitaLivrareBefore(StatusComanda status, LocalDate data);

    /** Încarcă o comandă împreună cu liniile ei într-o singură interogare (evită lazy loading). */
    @Query("select distinct c from ComandaTransport c left join fetch c.linii where c.id = :id")
    Optional<ComandaTransport> findCuLinii(@Param("id") Long id);
}
