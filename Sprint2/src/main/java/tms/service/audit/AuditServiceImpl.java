package tms.service.audit;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import tms.domain.entity.AbstractEntity;

@Service
public class AuditServiceImpl implements AuditService {

    private final Clock clock;
    private final List<AuditEvent> jurnal = new CopyOnWriteArrayList<>();

    public AuditServiceImpl(Clock clock) { this.clock = clock; }

    @Override
    public void inregistreazaCreare(AbstractEntity e, String utilizator) {
        LocalDateTime acum = LocalDateTime.now(clock);
        e.setCreatedByUser(utilizator);
        e.setDateCreated(acum);
        e.setUpdatedByUser(utilizator);
        e.setDateUpdated(acum);
        jurnal.add(new AuditEvent(e.getClass().getSimpleName(), e.getId(), "CREARE", utilizator, acum, null));
    }

    @Override
    public void inregistreazaModificare(AbstractEntity e, String utilizator, String detalii) {
        LocalDateTime acum = LocalDateTime.now(clock);
        e.setUpdatedByUser(utilizator);
        e.setDateUpdated(acum);
        jurnal.add(new AuditEvent(e.getClass().getSimpleName(), e.getId(), "MODIFICARE", utilizator, acum, detalii));
    }

    @Override
    public List<AuditEvent> istoric(AbstractEntity e) {
        return jurnal.stream()
                .filter(ev -> ev.tipEntitate().equals(e.getClass().getSimpleName()) && java.util.Objects.equals(ev.idEntitate(), e.getId()))
                .toList();
    }

    @Override
    public List<AuditEvent> toateEvenimentele() { return List.copyOf(jurnal); }
}
