package tms.domain.support;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import tms.domain.entity.Cmr;
import tms.domain.entity.ComandaTransport;
import tms.domain.entity.Ruta;
import tms.domain.enums.StatusCmr;

@Service
public class CmrFactoryImpl implements CmrFactory {

    private final SecventaCoduriService coduri;
    private final Clock clock;

    public CmrFactoryImpl(SecventaCoduriService coduri, Clock clock) { this.coduri = coduri; this.clock = clock; }

    @Override
    public Cmr emite(Ruta ruta, ComandaTransport comanda, BigDecimal greutateBruta) {
        Cmr cmr = new Cmr();
        cmr.setNumarCmr(coduri.urmatorulCod("CMR"));
        cmr.setRutaId(ruta.getId());
        cmr.setComandaId(comanda.getId());
        cmr.setGreutateBruta(greutateBruta);
        cmr.setDataEmiterii(LocalDateTime.now(clock));
        cmr.setStatus(StatusCmr.EMIS);
        return cmr;
    }
}
