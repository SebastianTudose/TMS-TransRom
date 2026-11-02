package tms.service.audit;

import java.time.LocalDateTime;

/** Înregistrare de audit: cine, când, ce acțiune, pe ce entitate. */
public record AuditEvent(String tipEntitate, Long idEntitate, String actiune, String utilizator,
                         LocalDateTime moment, String detalii) {
}
