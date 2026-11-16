package tms.domain.support;

import java.math.BigDecimal;
import tms.domain.entity.Cmr;
import tms.domain.entity.ComandaTransport;
import tms.domain.entity.Ruta;

/** Fabrică pentru agregatul Cmr (RB-26). */
public interface CmrFactory {
    Cmr emite(Ruta ruta, ComandaTransport comanda, BigDecimal greutateBruta);
}
