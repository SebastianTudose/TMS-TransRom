package tms.service.audit;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tms.domain.entity.AbstractEntity;
import tms.persistence.entity.AuditEntry;
import tms.persistence.repository.AuditEntryRepository;

@Service
@Transactional
public class AuditServiceImpl implements AuditService {

    private final Clock clock;
    private final AuditEntryRepository jurnal;

    public AuditServiceImpl(Clock clock, AuditEntryRepository jurnal) {
        this.clock = clock;
        this.jurnal = jurnal;
    }

    /** Numele tipului entității, fără sufixele claselor proxy generate de provider-ul JPA. */
    private static String tip(AbstractEntity e) {
        Class<?> c = e.getClass();
        while (c.getSimpleName().contains("$")) c = c.getSuperclass();
        return c.getSimpleName();
    }

    @Override
    public void inregistreazaCreare(AbstractEntity e, String utilizator) {
        LocalDateTime acum = LocalDateTime.now(clock);
        if (e.getCreatedByUser() == null) e.setCreatedByUser(utilizator);
        if (e.getDateCreated() == null) e.setDateCreated(acum);
        e.setUpdatedByUser(utilizator);
        e.setDateUpdated(acum);
        jurnal.save(new AuditEntry(tip(e), e.getId(), "CREARE", utilizator, acum, null));
    }

    @Override
    public void inregistreazaModificare(AbstractEntity e, String utilizator, String detalii) {
        LocalDateTime acum = LocalDateTime.now(clock);
        e.setUpdatedByUser(utilizator);
        e.setDateUpdated(acum);
        jurnal.save(new AuditEntry(tip(e), e.getId(), "MODIFICARE", utilizator, acum, detalii));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> istoric(AbstractEntity e) {
        return jurnal.findByTipEntitateAndIdEntitateOrderByIdAsc(tip(e), e.getId()).stream().map(AuditEntry::toEvent).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> toateEvenimentele() {
        return jurnal.findAllByOrderByIdAsc().stream().map(AuditEntry::toEvent).toList();
    }
}
