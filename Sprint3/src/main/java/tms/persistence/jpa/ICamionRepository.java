package tms.persistence.jpa;

import java.util.List;
import java.util.Optional;
import tms.domain.entity.Camion;
import tms.domain.enums.StatusCamion;
import tms.domain.vo.NrInmatriculare;

/** Repository „complet” pentru camioane: CRUD + căutări după număr și status. */
public interface ICamionRepository extends IRepository<Camion, Long> {

    Optional<Camion> findByNrInmatriculare(NrInmatriculare nrInmatriculare);

    List<Camion> findByStatusTehnic(StatusCamion status);
}
