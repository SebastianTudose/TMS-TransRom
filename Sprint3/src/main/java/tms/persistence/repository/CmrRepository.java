package tms.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tms.domain.entity.Cmr;
import tms.domain.enums.StatusCmr;

/** Repository pentru agregatul Cmr. */
public interface CmrRepository extends JpaRepository<Cmr, Long> {

    Optional<Cmr> findByNumarCmr(String numarCmr);

    List<Cmr> findByRutaId(Long rutaId);

    List<Cmr> findByComandaId(Long comandaId);

    Optional<Cmr> findByRutaIdAndComandaIdAndStatus(Long rutaId, Long comandaId, StatusCmr status);
}
