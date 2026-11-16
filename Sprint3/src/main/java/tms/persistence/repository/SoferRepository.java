package tms.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tms.domain.entity.Sofer;

/** Repository pentru șoferi (subclasă a utilizatorilor, rolul SOFER). */
public interface SoferRepository extends JpaRepository<Sofer, Long> {

    /** Șoferii activi al căror permis este valabil cel puțin până la data dată (RB-23). */
    List<Sofer> findByActivTrueAndDataExpirarePermisGreaterThanEqual(LocalDate data);
}
