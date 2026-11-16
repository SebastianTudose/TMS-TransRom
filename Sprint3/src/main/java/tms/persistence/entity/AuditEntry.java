package tms.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import tms.service.audit.AuditEvent;

/** Intrare persistentă în jurnalul de audit (RB-41): cine, când, ce acțiune, pe ce entitate. */
@Entity
@Table(name = "audit_entry",
        indexes = @Index(name = "ix_audit_entitate", columnList = "tip_entitate,id_entitate"))
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tip_entitate", nullable = false, length = 60)
    private String tipEntitate;

    @Column(name = "id_entitate")
    private Long idEntitate;

    @Column(name = "actiune", nullable = false, length = 20)
    private String actiune;

    @Column(name = "utilizator", length = 254)
    private String utilizator;

    @Column(name = "moment", nullable = false)
    private LocalDateTime moment;

    @Column(name = "detalii", length = 1000)
    private String detalii;

    protected AuditEntry() { }

    public AuditEntry(String tipEntitate, Long idEntitate, String actiune, String utilizator, LocalDateTime moment, String detalii) {
        this.tipEntitate = tipEntitate;
        this.idEntitate = idEntitate;
        this.actiune = actiune;
        this.utilizator = utilizator;
        this.moment = moment;
        this.detalii = detalii;
    }

    public Long getId() { return id; }

    public AuditEvent toEvent() {
        return new AuditEvent(tipEntitate, idEntitate, actiune, utilizator, moment, detalii);
    }
}
