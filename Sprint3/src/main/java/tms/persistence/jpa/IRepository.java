package tms.persistence.jpa;

import java.util.List;
import java.util.Optional;

/** Interfață CRUD generică pentru repository-urile JPA „complete” (varianta bonus, fără Spring Data). */
public interface IRepository<T, ID> {

    /** Persistă o entitate nouă sau actualizează una existentă; întoarce instanța gestionată. */
    T save(T entitate);

    Optional<T> findById(ID id);

    List<T> findAll();

    long count();

    boolean existsById(ID id);

    void delete(T entitate);

    void deleteById(ID id);
}
