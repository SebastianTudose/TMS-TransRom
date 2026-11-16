package tms.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tms.persistence.entity.AuditEntry;

/** Repository pentru jurnalul de audit. */
public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {

    List<AuditEntry> findByTipEntitateAndIdEntitateOrderByIdAsc(String tipEntitate, Long idEntitate);

    List<AuditEntry> findAllByOrderByIdAsc();
}
