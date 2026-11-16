package tms.persistence.listener;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import tms.domain.entity.AbstractEntity;

/** Listener de audit (RB-41): completează automat metadatele de creare și de modificare ale oricărei entități. */
public class AuditEntityListener {

    @PrePersist
    public void laCreare(AbstractEntity e) {
        LocalDateTime acum = AuditContext.acum();
        String utilizator = AuditContext.utilizatorCurent();
        if (e.getDateCreated() == null) e.setDateCreated(acum);
        if (e.getCreatedByUser() == null) e.setCreatedByUser(utilizator);
        if (e.getDateUpdated() == null) e.setDateUpdated(acum);
        if (e.getUpdatedByUser() == null) e.setUpdatedByUser(utilizator);
    }

    @PreUpdate
    public void laModificare(AbstractEntity e) {
        e.setDateUpdated(AuditContext.acum());
        String explicit = AuditContext.utilizatorExplicit();
        if (explicit != null) e.setUpdatedByUser(explicit);
        else if (e.getUpdatedByUser() == null) e.setUpdatedByUser(AuditContext.UTILIZATOR_SISTEM);
    }
}
