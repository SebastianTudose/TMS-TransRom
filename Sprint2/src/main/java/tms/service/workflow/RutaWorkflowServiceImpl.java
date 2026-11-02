package tms.service.workflow;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.stereotype.Service;
import tms.domain.entity.*;
import tms.domain.enums.*;
import tms.domain.exception.BusinessRuleException;
import tms.domain.support.CmrFactory;
import tms.domain.vo.SumaMonetara;
import tms.service.audit.AuditService;
import tms.service.calcul.CalculService;
import tms.service.validation.ValidationService;

@Service
public class RutaWorkflowServiceImpl implements RutaWorkflowService {

    private final ValidationService validare;
    private final CalculService calcul;
    private final AuditService audit;
    private final CmrFactory cmrFactory;
    private final Clock clock;

    public RutaWorkflowServiceImpl(ValidationService validare, CalculService calcul, AuditService audit,
                                   CmrFactory cmrFactory, Clock clock) {
        this.validare = validare; this.calcul = calcul; this.audit = audit; this.cmrFactory = cmrFactory; this.clock = clock;
    }

    private static String actor(Utilizator u) { return u.getEmail().valoare(); }

    // ---------------------------------------------------------------- UC-03
    @Override
    public Ruta planificaRuta(Utilizator u, Ruta ruta, Camion camion, Sofer sofer, Collection<Ruta> rutePeResurse) {
        validare.verificaAcces(u, Rol.DISPECER);
        ruta.setCamionId(camion.getId());
        ruta.setSoferId(sofer.getId());
        validare.valideazaPlanificare(ruta, camion, sofer, rutePeResurse);
        audit.inregistreazaCreare(ruta, actor(u));
        return ruta;
    }

    // ---------------------------------------------------------------- UC-04
    @Override
    public Cmr alocaComanda(Utilizator u, Ruta ruta, ComandaTransport comanda, Camion camion) {
        validare.verificaAcces(u, Rol.DISPECER);
        BigDecimal greutate = calcul.calculeazaGreutateComanda(comanda);
        validare.valideazaAlocare(ruta, comanda, camion, calcul.calculeazaGreutateAlocata(ruta).add(greutate));

        AlocareComanda alocare = new AlocareComanda();
        alocare.setComandaId(comanda.getId());
        alocare.setGreutateKg(greutate);
        ruta.addAlocare(alocare);

        validare.verificaTranzitie("RB-25", comanda.getStatus(), StatusComanda.PLANIFICATA);
        comanda.setStatus(StatusComanda.PLANIFICATA);

        Cmr cmr = cmrFactory.emite(ruta, comanda, greutate);                 // RB-26
        audit.inregistreazaCreare(cmr, actor(u));
        audit.inregistreazaModificare(ruta, actor(u), "Alocare comandă " + comanda.getNrComanda());
        audit.inregistreazaModificare(comanda, actor(u), "Alocată pe cursa " + ruta.getCodRuta());
        return cmr;
    }

    @Override
    public void dealocaComanda(Utilizator u, Ruta ruta, ComandaTransport comanda, Cmr cmr) {
        validare.verificaAcces(u, Rol.DISPECER);
        if (ruta.getStatus() != StatusRuta.PLANIFICATA)
            throw new BusinessRuleException("RB-25", "Dealocarea este permisă numai pe curse PLANIFICATE");
        AlocareComanda alocare = ruta.getAlocari().stream()
                .filter(a -> a.getComandaId().equals(comanda.getId())).findFirst()
                .orElseThrow(() -> new BusinessRuleException("RB-25", "Comanda nu este alocată pe această cursă"));
        validare.verificaTranzitie("RB-27", cmr.getStatus(), StatusCmr.ANULAT);   // CMR-ul se anulează, nu se șterge
        validare.verificaTranzitie("RB-25", comanda.getStatus(), StatusComanda.NOUA);
        ruta.removeAlocare(alocare);
        cmr.setStatus(StatusCmr.ANULAT);
        comanda.setStatus(StatusComanda.NOUA);
        audit.inregistreazaModificare(cmr, actor(u), "Anulare CMR la dealocare");
        audit.inregistreazaModificare(ruta, actor(u), "Dealocare comandă " + comanda.getNrComanda());
        audit.inregistreazaModificare(comanda, actor(u), "Dealocată de pe cursa " + ruta.getCodRuta());
    }

    // ---------------------------------------------------------------- UC-06
    @Override
    public void pornesteRuta(Utilizator u, Ruta ruta, Camion camion, Collection<ComandaTransport> comenzi, BigDecimal kmPlecare) {
        validare.verificaAcces(u, Rol.DISPECER);
        validare.valideazaPornire(ruta, camion, comenzi, kmPlecare);

        FoaieParcurs foaie = new FoaieParcurs();
        foaie.setDataEmiterii(LocalDateTime.now(clock));
        foaie.setKmPlecare(kmPlecare);
        foaie.setStatus(StatusDocument.INTOCMIT);
        ruta.setFoaieParcurs(foaie);
        ruta.setStatus(StatusRuta.IN_TRANZIT);
        camion.setStatusTehnic(StatusCamion.IN_CURSA);
        comenzi.forEach(c -> c.setStatus(StatusComanda.IN_EXECUTIE));
        audit.inregistreazaModificare(ruta, actor(u), "Pornire cursă, kmPlecare=" + kmPlecare);
        audit.inregistreazaModificare(camion, actor(u), "Status IN_CURSA");
        comenzi.forEach(c -> audit.inregistreazaModificare(c, actor(u), "Status IN_EXECUTIE"));
    }

    // ---------------------------------------------------------------- UC-07
    @Override
    public void finalizeazaRuta(Utilizator u, Ruta ruta, Camion camion, Collection<ComandaTransport> comenzi,
                                BigDecimal kmSosire, BigDecimal consumLitri, SumaMonetara pretPerLitru) {
        validare.verificaAcces(u, Rol.DISPECER);
        validare.valideazaFinalizare(ruta, comenzi);
        FoaieParcurs foaie = ruta.getFoaieParcurs();
        validare.valideazaDateSosire(foaie.getKmPlecare(), kmSosire, consumLitri, pretPerLitru);

        foaie.setKmSosire(kmSosire);
        foaie.setConsumCarburant(consumLitri);
        foaie.setStatus(StatusDocument.APROBAT);

        ruta.setDistantaParcursa(calcul.calculeazaDistanta(foaie));                       // RB-42
        ruta.setDataSosireReala(LocalDateTime.now(clock));

        CostCombustibil cost = new CostCombustibil();
        cost.setDataCalcul(LocalDateTime.now(clock));
        cost.setConsumTotal(consumLitri);
        cost.setPretPerLitru(pretPerLitru);
        cost.setCostTotal(calcul.calculeazaCostTotal(consumLitri, pretPerLitru));         // RB-43
        ruta.setCostCombustibil(cost);

        ruta.setStatus(StatusRuta.FINALIZATA);
        validare.verificaTranzitie("RB-29", camion.getStatusTehnic(), StatusCamion.DISPONIBIL);
        camion.setStatusTehnic(StatusCamion.DISPONIBIL);
        for (ComandaTransport c : comenzi) {
            validare.verificaTranzitie("RB-29", c.getStatus(), StatusComanda.LIVRATA);
            c.setStatus(StatusComanda.LIVRATA);
            audit.inregistreazaModificare(c, actor(u), "Status LIVRATA");
        }
        audit.inregistreazaCreare(cost, actor(u));
        audit.inregistreazaModificare(ruta, actor(u), "Finalizare cursă, distanța=" + ruta.getDistantaParcursa());
        audit.inregistreazaModificare(camion, actor(u), "Status DISPONIBIL");
    }

    // ---------------------------------------------------------------- UC-08
    @Override
    public void corecteazaFinalizare(Utilizator u, Ruta ruta, Collection<ComandaTransport> comenzi,
                                     BigDecimal kmSosire, BigDecimal consumLitri, SumaMonetara pretPerLitru) {
        validare.verificaAcces(u, Rol.DISPECER);
        validare.valideazaCorectareFinalizare(ruta, comenzi);
        FoaieParcurs foaie = ruta.getFoaieParcurs();
        validare.valideazaDateSosire(foaie.getKmPlecare(), kmSosire, consumLitri, pretPerLitru);

        foaie.setKmSosire(kmSosire);
        foaie.setConsumCarburant(consumLitri);
        ruta.setDistantaParcursa(calcul.calculeazaDistanta(foaie));
        CostCombustibil cost = ruta.getCostCombustibil();
        cost.setConsumTotal(consumLitri);
        cost.setPretPerLitru(pretPerLitru);
        cost.setCostTotal(calcul.calculeazaCostTotal(consumLitri, pretPerLitru));
        audit.inregistreazaModificare(ruta, actor(u), "Corectare finalizare");
        audit.inregistreazaModificare(cost, actor(u), "Recalculare cost: " + cost.getCostTotal().valoare());
    }

    @Override
    public void anuleazaFinalizarea(Utilizator u, Ruta ruta, Camion camion, Collection<ComandaTransport> comenzi) {
        validare.verificaAcces(u, Rol.DISPECER);
        validare.valideazaAnulareFinalizare(ruta, camion, comenzi);

        CostCombustibil costStornat = ruta.getCostCombustibil();
        ruta.setCostCombustibil(null);
        FoaieParcurs foaie = ruta.getFoaieParcurs();
        foaie.setKmSosire(null);
        foaie.setConsumCarburant(null);
        foaie.setStatus(StatusDocument.INTOCMIT);
        ruta.setDistantaParcursa(BigDecimal.ZERO);
        ruta.setDataSosireReala(null);
        ruta.setStatus(StatusRuta.IN_TRANZIT);
        camion.setStatusTehnic(StatusCamion.IN_CURSA);
        for (ComandaTransport c : comenzi) {
            validare.verificaTranzitie("RB-31", c.getStatus(), StatusComanda.IN_EXECUTIE);
            c.setStatus(StatusComanda.IN_EXECUTIE);
            audit.inregistreazaModificare(c, actor(u), "Status IN_EXECUTIE (anulare finalizare)");
        }
        audit.inregistreazaModificare(ruta, actor(u), "Anulare finalizare; cost stornat: " + costStornat.getCostTotal().valoare());
        audit.inregistreazaModificare(camion, actor(u), "Status IN_CURSA");
    }

    // ---------------------------------------------------------------- UC-09
    @Override
    public void anuleazaRuta(Utilizator u, Ruta ruta, Camion camion, Collection<ComandaTransport> comenzi, Collection<Cmr> cmrUri) {
        validare.verificaAcces(u, Rol.DISPECER);
        validare.valideazaAnulareRuta(ruta);

        if (ruta.getStatus() == StatusRuta.IN_TRANZIT) {
            camion.setStatusTehnic(StatusCamion.DISPONIBIL);
            if (ruta.getFoaieParcurs() != null) ruta.getFoaieParcurs().setStatus(StatusDocument.ANULAT);
        }
        ruta.setStatus(StatusRuta.ANULATA);
        for (Cmr cmr : cmrUri) {
            if (cmr.getStatus() == StatusCmr.EMIS) {
                cmr.setStatus(StatusCmr.ANULAT);
                audit.inregistreazaModificare(cmr, actor(u), "Anulare CMR (cursă anulată)");
            }
        }
        for (ComandaTransport c : comenzi) {
            validare.verificaTranzitie("RB-32", c.getStatus(), StatusComanda.NOUA);
            c.setStatus(StatusComanda.NOUA);
            audit.inregistreazaModificare(c, actor(u), "Revine NOUA (cursă anulată)");
        }
        audit.inregistreazaModificare(ruta, actor(u), "Anulare cursă");
    }
}
