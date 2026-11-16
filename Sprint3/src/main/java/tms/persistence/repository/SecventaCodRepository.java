package tms.persistence.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import tms.persistence.entity.SecventaCod;

/** Repository pentru contoarele codurilor unice. */
public interface SecventaCodRepository extends JpaRepository<SecventaCod, Long> {

    /** Citește contorul cu blocare pesimistă, ca două tranzacții să nu primească același număr. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SecventaCod> findByCheie(String cheie);
}
