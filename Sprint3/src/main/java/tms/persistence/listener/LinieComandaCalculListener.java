package tms.persistence.listener;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import tms.domain.entity.LinieComanda;
import tms.service.calcul.CalculServiceImpl;

/** Listener de calcul (RB-44): greutatea și volumul total ale liniei sunt recalculate la fiecare salvare. */
public class LinieComandaCalculListener {

    private final CalculServiceImpl calcul = new CalculServiceImpl();

    @PrePersist
    @PreUpdate
    public void recalculeaza(LinieComanda linie) {
        calcul.recalculeazaLinie(linie);
    }
}
