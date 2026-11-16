package tms.persistence.listener;

import jakarta.persistence.PrePersist;
import tms.domain.entity.Factura;
import tms.domain.entity.LinieFactura;
import tms.service.calcul.CalculServiceImpl;

/**
 * Listener de calcul (RB-45): valoarea totală a facturii rezultă din linii și discount.
 * Se execută doar la inserare: o factură emisă nu mai are linii modificabile, iar citirea colecției
 * lazy în timpul unui flush ar fi riscantă.
 */
public class FacturaCalculListener {

    private final CalculServiceImpl calcul = new CalculServiceImpl();

    @PrePersist
    public void calculeazaTotal(Factura factura) {
        if (factura.getLinii().isEmpty() || factura.getMoneda() == null || factura.getDiscountAplicat() == null) return;
        for (LinieFactura l : factura.getLinii()) {
            if (l.getValoare() == null || l.getValoare().valoare() == null) return;
        }
        factura.setValoareTotala(calcul.calculeazaValoareFactura(factura.getLinii(), factura.getDiscountAplicat(), factura.getMoneda()));
    }
}
