package tms.persistence.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tms.domain.entity.Camion;
import tms.domain.enums.StatusCamion;
import tms.domain.vo.NrInmatriculare;

/** Implementarea JPA a {@link ICamionRepository}, cu {@code EntityManager} injectat. */
@Repository
public class CamionRepositoryJPA extends AbstractRepositoryJPA<Camion, Long> implements ICamionRepository {

    public CamionRepositoryJPA() { super(Camion.class); }

    @Override
    @Transactional(readOnly = true)
    public Optional<Camion> findByNrInmatriculare(NrInmatriculare nr) {
        return em.createQuery("select c from Camion c where c.nrInmatriculare = :nr", Camion.class)
                .setParameter("nr", nr)
                .getResultStream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Camion> findByStatusTehnic(StatusCamion status) {
        return em.createQuery("select c from Camion c where c.statusTehnic = :s order by c.id", Camion.class)
                .setParameter("s", status)
                .getResultList();
    }
}
