package tms.persistence.listener;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import tms.domain.entity.CostCombustibil;
import tms.service.calcul.CalculServiceImpl;

/** Listener de calcul (RB-43): costTotal = consumTotal × pretPerLitru, recalculat la inserare și la modificare. */
public class CostCombustibilCalculListener {

    private final CalculServiceImpl calcul = new CalculServiceImpl();

    @PrePersist
    @PreUpdate
    public void recalculeaza(CostCombustibil cost) {
        if (cost.getConsumTotal() == null || cost.getPretPerLitru() == null || cost.getPretPerLitru().valoare() == null) return;
        cost.setCostTotal(calcul.calculeazaCostTotal(cost.getConsumTotal(), cost.getPretPerLitru()));
    }
}
