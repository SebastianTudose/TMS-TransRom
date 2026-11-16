package tms.persistence.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tms.domain.entity.Camion;
import tms.domain.enums.StatusCamion;
import tms.domain.vo.NrInmatriculare;

/** Repository pentru flota de camioane. */
public interface CamionRepository extends JpaRepository<Camion, Long> {

    Optional<Camion> findByNrInmatriculare(NrInmatriculare nrInmatriculare);

    boolean existsByNrInmatriculare(NrInmatriculare nrInmatriculare);

    List<Camion> findByStatusTehnic(StatusCamion status);

    /** Camioanele cu statusul dat și capacitate de cel puțin {@code minKg}, cele mai mici primele. */
    @Query("select c from Camion c where c.statusTehnic = :status and c.capacitateKg >= :minKg order by c.capacitateKg")
    List<Camion> findCuCapacitateMinima(@Param("status") StatusCamion status, @Param("minKg") BigDecimal minKg);
}
