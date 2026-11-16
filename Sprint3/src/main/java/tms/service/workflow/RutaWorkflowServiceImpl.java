package tms.service.workflow;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tms.domain.entity.AlocareComanda;
import tms.domain.entity.Camion;
import tms.domain.entity.Cmr;
import tms.domain.entity.ComandaTransport;
import tms.domain.entity.CostCombustibil;
import tms.domain.entity.FoaieParcurs;
import tms.domain.entity.Ruta;
import tms.domain.entity.Sofer;
import tms.domain.entity.Utilizator;
import tms.domain.enums.Rol;
import tms.domain.enums.StatusCamion;
import tms.domain.enums.StatusCmr;
import tms.domain.enums.StatusComanda;
import tms.domain.enums.StatusDocument;
import tms.domain.enums.StatusRuta;
import tms.domain.exception.BusinessRuleException;
import tms.domain.support.CmrFactory;
import tms.domain.vo.IntervalTimp;
import tms.domain.vo.SumaMonetara;
import tms.persistence.listener.AuditContext;
import tms.persistence.repository.CamionRepository;
import tms.persistence.repository.CmrRepository;
import tms.persistence.repository.ComandaTransportRepository;
import tms.persistence.repository.RutaRepository;
import tms.service.audit.AuditService;
import tms.service.calcul.CalculService;
import tms.service.validation.ValidationService;

@Service
@Transactional
public class RutaWorkflowServiceImpl implements RutaWorkflowService {

    private static final List<StatusRuta> STATUSURI_ACTIVE = List.of(StatusRuta.PLANIFICATA, StatusRuta.IN_TRANZIT);

    private final ValidationService validare;
    private final CalculService calcul;
    private final AuditService audit;
    private final CmrFactory cmrFactory;
    private final Clock clock;
    private final RutaRepository rute;
    private final CamionRepository camioane;
    private final ComandaTransportRepository comenzi;
    private final CmrRepository cmrUri;

    public RutaWorkflowServiceImpl(ValidationService validare, CalculService calcul, AuditService audit,
                                   CmrFactory cmrFactory, Clock clock, RutaRepository rute, CamionRepository camioane,
                                   ComandaTransportRepository comenzi, CmrRepository cmrUri) {
        this.validare = validare; this.calcul = calcul; this.audit = audit; this.cmrFactory = cmrFactory; this.clock = clock;
        this.rute = rute; this.camioane = camioane; this.comenzi = comenzi; this.cmrUri = cmrUri;
    }

    private static String actor(Utilizator u) { return u.getEmail().valoare(); }

    // ---------------------------------------------------------------- încărcări din repository-uri
    private Camion camionul(Ruta ruta) {
        return camioane.findById(ruta.getCamionId())
                .orElseThrow(() -> new BusinessRuleException("RB-18", "Camionul cursei nu există"));
    }

    private List<ComandaTransport> comenzileAlocate(Ruta ruta) {
        List<Long> ids = ruta.getAlocari().stream().map(AlocareComanda::getComandaId).toList();
        return ids.isEmpty() ? List.of() : comenzi.findAllById(ids);
    }

    /** Cursele active ale aceluiași camion sau șofer, care se suprapun cu intervalul cursei (RB-22). */
    private List<Ruta> cursePeResurse(Ruta ruta, Camion camion, Sofer sofer) {
        IntervalTimp i = ruta.getIntervalPlanificat();
        if (i == null || i.inceput() == null || i.sfarsit() == null) return List.of();   // regulile de câmp vor semnala eroarea
        return rute.findSuprapuse(STATUSURI_ACTIVE, camion.getId(), sofer.getId(), i.inceput(), i.sfarsit());
    }

    // ---------------------------------------------------------------- UC-03
    @Override
    public Ruta planificaRuta(Utilizator u, Ruta ruta, Camion camion, Sofer sofer) {
        validare.verificaAcces(u, Rol.DISPECER);
        ruta.setCamionId(camion.getId());
        ruta.setSoferId(sofer.getId());
        validare.valideazaPlanificare(ruta, camion, sofer, cursePeResurse(ruta, camion, sofer));
        Ruta salvata = AuditContext.ca(actor(u), () -> rute.save(ruta));
        audit.inregistreazaCreare(salvata, actor(u));
        return salvata;
    }

    // ---------------------------------------------------------------- UC-04
    @Override
    public Cmr alocaComanda(Utilizator u, Ruta ruta, ComandaTransport comanda) {
        validare.verificaAcces(u, Rol.DISPECER);
        Camion camion = camionul(ruta);
        BigDecimal greutate = calcul.calculeazaGreutateComanda(comanda);
        validare.valideazaAlocare(ruta, comanda, camion, calcul.calculeazaGreutateAlocata(ruta).add(greutate));
        validare.verificaTranzitie("RB-25", comanda.getStatus(), StatusComanda.PLANIFICATA);
        AlocareComanda alocare = new AlocareComanda();
        alocare.setComandaId(comanda.getId());
        alocare.setGreutateKg(greutate);
        ruta.addAlocare(alocare);
        comanda.setStatus(StatusComanda.PLANIFICATA);
        Ruta rutaSalvata = AuditContext.ca(actor(u), () -> rute.save(ruta));
        ComandaTransport comandaSalvata = comenzi.save(comanda);
        Cmr cmr = AuditContext.ca(actor(u), () -> cmrUri.save(cmrFactory.emite(rutaSalvata, comandaSalvata, greutate)));  // RB-26
        audit.inregistreazaCreare(cmr, actor(u));
        audit.inregistreazaModificare(rutaSalvata, actor(u), "Alocare comandă " + comanda.getNrComanda());
        audit.inregistreazaModificare(comandaSalvata, actor(u), "Alocată pe cursa " + ruta.getCodRuta());
        return cmr;
    }

    @Override
    public void dealocaComanda(Utilizator u, Ruta ruta, ComandaTransport comanda) {
        validare.verificaAcces(u, Rol.DISPECER);
        if (ruta.getStatus() != StatusRuta.PLANIFICATA)
            throw new BusinessRuleException("RB-25", "Dealocarea este permisă numai pe curse PLANIFICATE");
        AlocareComanda alocare = ruta.getAlocari().stream()
                .filter(a -> a.getComandaId().equals(comanda.getId())).findFirst()
                .orElseThrow(() -> new BusinessRuleException("RB-25", "Comanda nu este alocată pe această cursă"));
        Cmr cmr = cmrUri.findByRutaIdAndComandaIdAndStatus(ruta.getId(), comanda.getId(), StatusCmr.EMIS)
                .orElseThrow(() -> new BusinessRuleException("RB-27", "Nu există un CMR emis pentru această alocare"));
        validare.verificaTranzitie("RB-27", cmr.getStatus(), StatusCmr.ANULAT);   // CMR-ul se anulează, nu se șterge
        validare.verificaTranzitie("RB-25", comanda.getStatus(), StatusComanda.NOUA);
        ruta.removeAlocare(alocare);
        cmr.setStatus(StatusCmr.ANULAT);
        comanda.setStatus(StatusComanda.NOUA);
        Ruta rutaSalvata = rute.save(ruta);
        Cmr cmrSalvat = cmrUri.save(cmr);
        ComandaTransport comandaSalvata = comenzi.save(comanda);
        audit.inregistreazaModificare(cmrSalvat, actor(u), "Anulare CMR la dealocare");
        audit.inregistreazaModificare(rutaSalvata, actor(u), "Dealocare comandă " + comanda.getNrComanda());
        audit.inregistreazaModificare(comandaSalvata, actor(u), "Dealocată de pe cursa " + ruta.getCodRuta());
    }

    // ---------------------------------------------------------------- UC-06
    @Override
    public void pornesteRuta(Utilizator u, Ruta ruta, BigDecimal kmPlecare) {
        validare.verificaAcces(u, Rol.DISPECER);
        Camion camion = camionul(ruta);
        List<ComandaTransport> alocate = comenzileAlocate(ruta);
        validare.valideazaPornire(ruta, camion, alocate, kmPlecare);
        FoaieParcurs foaie = new FoaieParcurs();
        foaie.setDataEmiterii(LocalDateTime.now(clock));
        foaie.setKmPlecare(kmPlecare);
        foaie.setStatus(StatusDocument.INTOCMIT);
        ruta.setFoaieParcurs(foaie);
        ruta.setStatus(StatusRuta.IN_TRANZIT);
        camion.setStatusTehnic(StatusCamion.IN_CURSA);
        alocate.forEach(c -> c.setStatus(StatusComanda.IN_EXECUTIE));
        Ruta rutaSalvata = AuditContext.ca(actor(u), () -> rute.save(ruta));
        camioane.save(camion);
        comenzi.saveAll(alocate);
        audit.inregistreazaModificare(rutaSalvata, actor(u), "Pornire cursă, kmPlecare=" + kmPlecare);
        audit.inregistreazaModificare(camion, actor(u), "Status IN_CURSA");
        alocate.forEach(c -> audit.inregistreazaModificare(c, actor(u), "Status IN_EXECUTIE"));
    }

    // ---------------------------------------------------------------- UC-07
    @Override
    public void finalizeazaRuta(Utilizator u, Ruta ruta, BigDecimal kmSosire, BigDecimal consumLitri, SumaMonetara pretPerLitru) {
        validare.verificaAcces(u, Rol.DISPECER);
        Camion camion = camionul(ruta);
        List<ComandaTransport> alocate = comenzileAlocate(ruta);
        validare.valideazaFinalizare(ruta, alocate);
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
        for (ComandaTransport c : alocate) {
            validare.verificaTranzitie("RB-29", c.getStatus(), StatusComanda.LIVRATA);
            c.setStatus(StatusComanda.LIVRATA);
        }
        Ruta rutaSalvata = AuditContext.ca(actor(u), () -> rute.save(ruta));
        camioane.save(camion);
        comenzi.saveAll(alocate);
        alocate.forEach(c -> audit.inregistreazaModificare(c, actor(u), "Status LIVRATA"));
        audit.inregistreazaCreare(rutaSalvata.getCostCombustibil(), actor(u));
        audit.inregistreazaModificare(rutaSalvata, actor(u), "Finalizare cursă, distanța=" + rutaSalvata.getDistantaParcursa());
        audit.inregistreazaModificare(camion, actor(u), "Status DISPONIBIL");
    }

    // ---------------------------------------------------------------- UC-08
    @Override
    public void corecteazaFinalizare(Utilizator u, Ruta ruta, BigDecimal kmSosire, BigDecimal consumLitri, SumaMonetara pretPerLitru) {
        validare.verificaAcces(u, Rol.DISPECER);
        validare.valideazaCorectareFinalizare(ruta, comenzileAlocate(ruta));
        FoaieParcurs foaie = ruta.getFoaieParcurs();
        validare.valideazaDateSosire(foaie.getKmPlecare(), kmSosire, consumLitri, pretPerLitru);
        foaie.setKmSosire(kmSosire);
        foaie.setConsumCarburant(consumLitri);
        ruta.setDistantaParcursa(calcul.calculeazaDistanta(foaie));
        CostCombustibil cost = ruta.getCostCombustibil();
        cost.setConsumTotal(consumLitri);
        cost.setPretPerLitru(pretPerLitru);
        cost.setCostTotal(calcul.calculeazaCostTotal(consumLitri, pretPerLitru));
        Ruta rutaSalvata = rute.save(ruta);
        audit.inregistreazaModificare(rutaSalvata, actor(u), "Corectare finalizare");
        audit.inregistreazaModificare(rutaSalvata.getCostCombustibil(), actor(u),
                "Recalculare cost: " + rutaSalvata.getCostCombustibil().getCostTotal().valoare());
    }

    @Override
    public void anuleazaFinalizarea(Utilizator u, Ruta ruta) {
        validare.verificaAcces(u, Rol.DISPECER);
        Camion camion = camionul(ruta);
        List<ComandaTransport> alocate = comenzileAlocate(ruta);
        validare.valideazaAnulareFinalizare(ruta, camion, alocate);
        CostCombustibil costStornat = ruta.getCostCombustibil();
        ruta.setCostCombustibil(null);                                                    // orphanRemoval șterge costul
        FoaieParcurs foaie = ruta.getFoaieParcurs();
        foaie.setKmSosire(null);
        foaie.setConsumCarburant(null);
        foaie.setStatus(StatusDocument.INTOCMIT);
        ruta.setDistantaParcursa(BigDecimal.ZERO);
        ruta.setDataSosireReala(null);
        ruta.setStatus(StatusRuta.IN_TRANZIT);
        camion.setStatusTehnic(StatusCamion.IN_CURSA);
        for (ComandaTransport c : alocate) {
            validare.verificaTranzitie("RB-31", c.getStatus(), StatusComanda.IN_EXECUTIE);
            c.setStatus(StatusComanda.IN_EXECUTIE);
        }
        Ruta rutaSalvata = rute.save(ruta);
        camioane.save(camion);
        comenzi.saveAll(alocate);
        alocate.forEach(c -> audit.inregistreazaModificare(c, actor(u), "Status IN_EXECUTIE (anulare finalizare)"));
        audit.inregistreazaModificare(rutaSalvata, actor(u), "Anulare finalizare; cost stornat: " + costStornat.getCostTotal().valoare());
        audit.inregistreazaModificare(camion, actor(u), "Status IN_CURSA");
    }

    // ---------------------------------------------------------------- UC-09
    @Override
    public void anuleazaRuta(Utilizator u, Ruta ruta) {
        validare.verificaAcces(u, Rol.DISPECER);
        validare.valideazaAnulareRuta(ruta);
        Camion camion = camionul(ruta);
        List<ComandaTransport> alocate = comenzileAlocate(ruta);
        Collection<Cmr> cmrDeAnulat = cmrUri.findByRutaId(ruta.getId());
        if (ruta.getStatus() == StatusRuta.IN_TRANZIT) {
            camion.setStatusTehnic(StatusCamion.DISPONIBIL);
            if (ruta.getFoaieParcurs() != null) ruta.getFoaieParcurs().setStatus(StatusDocument.ANULAT);
        }
        ruta.setStatus(StatusRuta.ANULATA);
        for (Cmr cmr : cmrDeAnulat) {
            if (cmr.getStatus() == StatusCmr.EMIS) {
                cmr.setStatus(StatusCmr.ANULAT);
                audit.inregistreazaModificare(cmr, actor(u), "Anulare CMR (cursă anulată)");
            }
        }
        for (ComandaTransport c : alocate) {
            validare.verificaTranzitie("RB-32", c.getStatus(), StatusComanda.NOUA);
            c.setStatus(StatusComanda.NOUA);
            audit.inregistreazaModificare(c, actor(u), "Revine NOUA (cursă anulată)");
        }
        Ruta rutaSalvata = rute.save(ruta);
        camioane.save(camion);
        cmrUri.saveAll(cmrDeAnulat);
        comenzi.saveAll(alocate);
        audit.inregistreazaModificare(rutaSalvata, actor(u), "Anulare cursă");
    }
}
