package tms.service.audit;

import java.util.List;
import tms.domain.entity.AbstractEntity;

/** Serviciu de audit (RB-41): completează metadatele entității și păstrează jurnalul evenimentelor. */
public interface AuditService {
    void inregistreazaCreare(AbstractEntity entitate, String utilizator);
    void inregistreazaModificare(AbstractEntity entitate, String utilizator, String detalii);
    List<AuditEvent> istoric(AbstractEntity entitate);
    List<AuditEvent> toateEvenimentele();
}
