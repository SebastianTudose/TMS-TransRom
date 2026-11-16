package tms.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tms.domain.entity.Utilizator;
import tms.domain.vo.AdresaEmail;

/** Repository pentru conturile de utilizator (toate rolurile, un singur tabel cu discriminator). */
public interface UtilizatorRepository extends JpaRepository<Utilizator, Long> {

    Optional<Utilizator> findByEmail(AdresaEmail email);

    boolean existsByEmail(AdresaEmail email);

    List<Utilizator> findByActivTrue();
}
