package tms.domain.support;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import tms.domain.entity.Client;
import tms.domain.entity.ComandaTransport;
import tms.domain.entity.Factura;
import tms.domain.entity.LinieFactura;
import tms.domain.enums.StatusFactura;
import tms.service.calcul.CalculService;

@Service
public class FacturaFactoryImpl implements FacturaFactory {

    private final SecventaCoduriService coduri;
    private final CalculService calcul;
    private final Clock clock;

    public FacturaFactoryImpl(SecventaCoduriService coduri, CalculService calcul, Clock clock) {
        this.coduri = coduri; this.calcul = calcul; this.clock = clock;
    }

    @Override
    public Factura creeaza(Client client, List<ComandaTransport> comenzi, LocalDate dataScadenta) {
        Factura f = new Factura();
        f.setNumar(coduri.urmatorulCod("FCT"));
        f.setClientId(client.getId());
        f.setDataEmiterii(LocalDate.now(clock));
        f.setDataScadenta(dataScadenta);
        f.setMoneda(client.getMonedaFacturare());
        f.setDiscountAplicat(client.getDiscountAcordat());
        f.setStatus(StatusFactura.EMISA);
        for (ComandaTransport c : comenzi) {
            LinieFactura l = new LinieFactura();
            l.setComandaId(c.getId());
            l.setValoare(c.getTarifAgreat());
            f.addLinie(l);
        }
        f.setValoareTotala(calcul.calculeazaValoareFactura(f.getLinii(), f.getDiscountAplicat(), f.getMoneda()));
        return f;
    }
}
