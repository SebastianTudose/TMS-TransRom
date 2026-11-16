package tms.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementare JPA de bază a operațiilor CRUD, scrisă direct peste {@link EntityManager}.
 * Clasele concrete adaugă interogările specifice entității.
 */
@Transactional
public abstract class AbstractRepositoryJPA<T, ID> implements IRepository<T, ID> {

    @PersistenceContext
    protected EntityManager em;

    private final Class<T> tip;

    protected AbstractRepositoryJPA(Class<T> tip) { this.tip = tip; }

    @Override
    public T save(T entitate) {
        Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entitate);
        if (id == null) {
            em.persist(entitate);
            return entitate;
        }
        return em.merge(entitate);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(ID id) { return Optional.ofNullable(em.find(tip, id)); }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return em.createQuery("select e from " + tip.getSimpleName() + " e", tip).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return em.createQuery("select count(e) from " + tip.getSimpleName() + " e", Long.class).getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ID id) { return em.find(tip, id) != null; }

    @Override
    public void delete(T entitate) { em.remove(em.contains(entitate) ? entitate : em.merge(entitate)); }

    @Override
    public void deleteById(ID id) { findById(id).ifPresent(this::delete); }
}
