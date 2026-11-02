package tms.service.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import tms.domain.entity.*;
import tms.domain.enums.*;
import tms.domain.exception.BusinessRuleException;
import tms.domain.exception.BusinessRuleViolation;
import tms.domain.vo.AdresaEmail;
import tms.domain.vo.SumaMonetara;

@Service
public class ValidationServiceImpl implements ValidationService {

    private final Validator validator;
    private final Clock clock;

    public ValidationServiceImpl(Validator validator, Clock clock) {
        this.validator = validator;
        this.clock = clock;
    }

    // ------------------------------------------------------------ infrastructură
    private List<BusinessRuleViolation> violatii(Object entitate) {
        List<BusinessRuleViolation> rezultat = new ArrayList<>();
        for (ConstraintViolation<Object> cv : validator.validate(entitate)) {
            String mesaj = cv.getMessage();
            int i = mesaj.indexOf(':');
            if (mesaj.startsWith("RB-") && i > 0) rezultat.add(new BusinessRuleViolation(mesaj.substring(0, i), mesaj.substring(i + 1).trim()));
            else rezultat.add(new BusinessRuleViolation("RB-VAL", cv.getPropertyPath() + " " + mesaj));
        }
        rezultat.sort(Comparator.comparing(BusinessRuleViolation::cod).thenComparing(BusinessRuleViolation::mesaj));
        return rezultat;
    }

    private static void arunca(List<BusinessRuleViolation> v) {
        if (!v.isEmpty()) throw new BusinessRuleException(v);
    }

    private static void cerinta(List<BusinessRuleViolation> v, boolean conditie, String cod, String mesaj) {
        if (!conditie) v.add(new BusinessRuleViolation(cod, mesaj));
    }

    @Override
    public <T> void valideazaEntitate(T entitate) { arunca(violatii(entitate)); }

    // ------------------------------------------------------------ acces și unicitate
    @Override
    public void verificaAcces(Utilizator u, Rol... roluriPermise) {
        if (u == null || !u.isActiv())
            throw new BusinessRuleException("RB-39", "Accesul necesită un cont activ");
        if (!Arrays.asList(roluriPermise).contains(u.getRol()))
            throw new BusinessRuleException("RB-39", "Rolul " + u.getRol() + " nu are dreptul de a efectua operația");
    }

    @Override
    public void verificaEmailUnic(AdresaEmail email, Collection<? extends Utilizator> existente) {
        boolean dublura = existente.stream().anyMatch(u -> u.getEmail().valoare().equalsIgnoreCase(email.valoare()));
        if (dublura) throw new BusinessRuleException("RB-01", "Adresa de email există deja în sistem");
    }

    @Override
    public void verificaCodFiscalUnic(Client client, Collection<Client> existenti) {
        boolean dublura = existenti.stream().anyMatch(c -> c != client && c.getCodFiscal().equals(client.getCodFiscal()));
        if (dublura) throw new BusinessRuleException("RB-03", "Codul fiscal există deja în sistem");
    }

    @Override
    public <E extends Enum<E> & StareCuTranzitii<E>> void verificaTranzitie(String cod, E curent, E tinta) {
        if (!curent.poateTrecaIn(tinta))
            throw new BusinessRuleException(cod, "Tranziție de stare nepermisă: " + curent + " -> " + tinta);
    }

    // ------------------------------------------------------------ agregate
    @Override
    public void valideazaComanda(ComandaTransport c) {
        List<BusinessRuleViolation> v = violatii(c);
        if (c.getLocatieIncarcare() != null && c.getLocatieIncarcare().equals(c.getLocatieDescarcare()))
            v.add(new BusinessRuleViolation("RB-16", "Locația de încărcare trebuie să difere de cea de descărcare"));
        if (c.getTarifAgreat() != null && c.getTarifAgreat().valoare() != null && c.getTarifAgreat().valoare().signum() <= 0)
            v.add(new BusinessRuleViolation("RB-16", "Tariful agreat trebuie să fie mai mare decât zero"));
        arunca(v);
    }

    @Override
    public void valideazaFactura(Factura f, Client client) {
        List<BusinessRuleViolation> v = violatii(f);
        cerinta(v, f.getMoneda() == client.getMonedaFacturare(), "RB-17", "Factura trebuie emisă în moneda de facturare a clientului");
        boolean omogena = f.getLinii().stream().allMatch(l -> l.getValoare() != null && l.getValoare().moneda() == f.getMoneda());
        cerinta(v, omogena, "RB-17", "Toate valorile unei facturi trebuie să fie în aceeași monedă");
        arunca(v);
    }

    // ------------------------------------------------------------ workflow cursă
    @Override
    public void valideazaPlanificare(Ruta r, Camion camion, Sofer sofer, Collection<Ruta> rutePeResurse) {
        List<BusinessRuleViolation> v = violatii(r);
        cerinta(v, r.getStatus() == StatusRuta.PLANIFICATA, "RB-24", "O cursă nouă pornește cu statusul PLANIFICATA");
        if (r.getLocatiePlecare() != null && r.getLocatiePlecare().equals(r.getLocatieDestinatie()))
            v.add(new BusinessRuleViolation("RB-18", "Plecarea trebuie să difere de destinație"));
        cerinta(v, camion.getStatusTehnic() != StatusCamion.IN_SERVICE, "RB-21", "Camionul este în service și nu poate fi alocat");
        if (r.getIntervalPlanificat() != null && r.getIntervalPlanificat().inceput() != null
                && r.getIntervalPlanificat().sfarsit() != null && r.getIntervalPlanificat().isSfarsitDupaInceput()) {
            cerinta(v, r.getIntervalPlanificat().inceput().isAfter(LocalDateTime.now(clock)), "RB-24", "Data plecării trebuie să fie în viitor");
            boolean conflict = rutePeResurse.stream()
                    .filter(o -> o != r && o.getStatus().esteActiva() && o.getIntervalPlanificat() != null)
                    .filter(o -> Objects.equals(o.getCamionId(), camion.getId()) || Objects.equals(o.getSoferId(), sofer.getId()))
                    .anyMatch(o -> o.getIntervalPlanificat().suprapune(r.getIntervalPlanificat()));
            cerinta(v, !conflict, "RB-22", "Camionul sau șoferul au deja o cursă activă în intervalul selectat");
            if (sofer.getDataExpirarePermis() != null)
                cerinta(v, !sofer.getDataExpirarePermis().isBefore(r.getIntervalPlanificat().sfarsit().toLocalDate()),
                        "RB-23", "Permisul șoferului expiră înainte de sosirea estimată");
        }
        arunca(v);
    }

    @Override
    public void valideazaAlocare(Ruta r, ComandaTransport comanda, Camion camion, BigDecimal greutateDupaAlocare) {
        List<BusinessRuleViolation> v = new ArrayList<>();
        cerinta(v, r.getStatus() == StatusRuta.PLANIFICATA, "RB-25", "Comenzile se alocă numai pe curse PLANIFICATE");
        cerinta(v, comanda.getStatus() == StatusComanda.NOUA, "RB-25", "Se poate aloca numai o comandă cu statusul NOUA");
        cerinta(v, r.getAlocari().stream().noneMatch(a -> Objects.equals(a.getComandaId(), comanda.getId())),
                "RB-25", "Comanda este deja alocată pe această cursă");
        cerinta(v, greutateDupaAlocare.compareTo(camion.getCapacitateKg()) <= 0,
                "RB-19", "Greutatea alocată (" + greutateDupaAlocare + " kg) depășește capacitatea camionului (" + camion.getCapacitateKg() + " kg)");
        arunca(v);
    }

    private static void comenziPotrivite(List<BusinessRuleViolation> v, Ruta r, Collection<ComandaTransport> comenzi, String cod) {
        Set<Long> alocate = new HashSet<>();
        r.getAlocari().forEach(a -> alocate.add(a.getComandaId()));
        Set<Long> primite = new HashSet<>();
        comenzi.forEach(c -> primite.add(c.getId()));
        cerinta(v, alocate.equals(primite), cod, "Comenzile furnizate nu corespund alocărilor cursei");
    }

    @Override
    public void valideazaPornire(Ruta r, Camion camion, Collection<ComandaTransport> comenzi, BigDecimal kmPlecare) {
        List<BusinessRuleViolation> v = new ArrayList<>();
        cerinta(v, r.getStatus() == StatusRuta.PLANIFICATA, "RB-28", "Doar o cursă PLANIFICATA poate fi pornită");
        cerinta(v, camion.getStatusTehnic() == StatusCamion.DISPONIBIL, "RB-28", "Camionul trebuie să fie DISPONIBIL la pornire");
        cerinta(v, !r.getAlocari().isEmpty(), "RB-28", "Cursa trebuie să aibă cel puțin o comandă alocată");
        cerinta(v, kmPlecare != null && kmPlecare.signum() >= 0, "RB-28", "Kilometrajul de plecare este obligatoriu și nenegativ");
        comenziPotrivite(v, r, comenzi, "RB-28");
        arunca(v);
    }

    @Override
    public void valideazaDateSosire(BigDecimal kmPlecare, BigDecimal kmSosire, BigDecimal consum, SumaMonetara pret) {
        List<BusinessRuleViolation> v = new ArrayList<>();
        cerinta(v, kmSosire != null && kmSosire.compareTo(kmPlecare) >= 0, "RB-13", "Kilometrajul de sosire trebuie să fie cel puțin egal cu cel de plecare");
        cerinta(v, consum != null && consum.signum() >= 0, "RB-14", "Consumul trebuie să fie nenegativ");
        cerinta(v, pret != null && pret.valoare() != null && pret.valoare().signum() >= 0 && pret.moneda() != null, "RB-14", "Prețul per litru trebuie să fie nenegativ");
        arunca(v);
    }

    @Override
    public void valideazaFinalizare(Ruta r, Collection<ComandaTransport> comenzi) {
        List<BusinessRuleViolation> v = new ArrayList<>();
        cerinta(v, r.getStatus() == StatusRuta.IN_TRANZIT, "RB-29", "Doar o cursă IN_TRANZIT poate fi finalizată");
        cerinta(v, r.getFoaieParcurs() != null && r.getFoaieParcurs().getStatus() == StatusDocument.INTOCMIT,
                "RB-29", "Cursa trebuie să aibă o foaie de parcurs în starea INTOCMIT");
        comenziPotrivite(v, r, comenzi, "RB-29");
        arunca(v);
    }

    @Override
    public void valideazaCorectareFinalizare(Ruta r, Collection<ComandaTransport> comenzi) {
        List<BusinessRuleViolation> v = new ArrayList<>();
        cerinta(v, r.getStatus() == StatusRuta.FINALIZATA && r.getCostCombustibil() != null, "RB-30", "Doar o cursă FINALIZATA poate fi corectată");
        cerinta(v, comenzi.stream().noneMatch(c -> c.getStatus() == StatusComanda.FACTURATA), "RB-30", "Cursa conține comenzi deja facturate");
        arunca(v);
    }

    @Override
    public void valideazaAnulareFinalizare(Ruta r, Camion camion, Collection<ComandaTransport> comenzi) {
        List<BusinessRuleViolation> v = new ArrayList<>();
        cerinta(v, r.getStatus() == StatusRuta.FINALIZATA, "RB-31", "Doar o cursă FINALIZATA poate avea finalizarea anulată");
        cerinta(v, camion.getStatusTehnic() == StatusCamion.DISPONIBIL, "RB-31", "Camionul a fost deja realocat sau este în service");
        cerinta(v, comenzi.stream().noneMatch(c -> c.getStatus() == StatusComanda.FACTURATA), "RB-31", "Cursa conține comenzi deja facturate");
        arunca(v);
    }

    @Override
    public void valideazaAnulareRuta(Ruta r) {
        if (!r.getStatus().esteActiva())
            throw new BusinessRuleException("RB-32", "Pot fi anulate numai cursele PLANIFICATE sau IN_TRANZIT");
    }
}
