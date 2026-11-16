package tms.persistence.listener;

import jakarta.persistence.PreRemove;
import tms.domain.exception.BusinessRuleException;

/** Listener de integritate (RB-33): documentele (cursă, foaie de parcurs, CMR, factură) nu se șterg fizic. */
public class NoDeleteListener {

    @PreRemove
    public void interzice(Object entitate) {
        throw new BusinessRuleException("RB-33",
                "Documentele nu se șterg fizic, ci se anulează sau se storneză (" + entitate.getClass().getSimpleName() + ")");
    }
}
