package tms.service.validation;

import java.math.BigDecimal;
import java.util.Collection;
import tms.domain.entity.*;
import tms.domain.enums.Rol;
import tms.domain.enums.StareCuTranzitii;
import tms.domain.vo.AdresaEmail;
import tms.domain.vo.SumaMonetara;

/**
 * Servicii de validare ale domeniului. Regulile pe câmpuri sunt declarate prin adnotări
 * JavaBean Validation în entități; regulile care implică mai multe obiecte sunt aici.
 * Toate metodele aruncă BusinessRuleException cu codul regulii încălcate.
 */
public interface ValidationService {
    <T> void valideazaEntitate(T entitate);                                         // RB-01..RB-15 (adnotări)
    void verificaAcces(Utilizator utilizator, Rol... roluriPermise);                // RB-39
    void verificaEmailUnic(AdresaEmail email, Collection<? extends Utilizator> existente);   // RB-01
    void verificaCodFiscalUnic(Client client, Collection<Client> existenti);        // RB-03
    <E extends Enum<E> & StareCuTranzitii<E>> void verificaTranzitie(String cod, E curent, E tinta);

    void valideazaComanda(ComandaTransport comanda);                                // RB-16
    void valideazaFactura(Factura factura, Client client);                          // RB-17
    void valideazaPlanificare(Ruta ruta, Camion camion, Sofer sofer, Collection<Ruta> rutePeResurse); // RB-18, 21-24
    void valideazaAlocare(Ruta ruta, ComandaTransport comanda, Camion camion, BigDecimal greutateDupaAlocare); // RB-19, 25
    void valideazaPornire(Ruta ruta, Camion camion, Collection<ComandaTransport> comenzi, BigDecimal kmPlecare); // RB-28
    void valideazaDateSosire(BigDecimal kmPlecare, BigDecimal kmSosire, BigDecimal consum, SumaMonetara pretPerLitru); // RB-13, RB-14
    void valideazaFinalizare(Ruta ruta, Collection<ComandaTransport> comenzi);     // RB-29
    void valideazaCorectareFinalizare(Ruta ruta, Collection<ComandaTransport> comenzi);   // RB-30
    void valideazaAnulareFinalizare(Ruta ruta, Camion camion, Collection<ComandaTransport> comenzi); // RB-31
    void valideazaAnulareRuta(Ruta ruta);                                           // RB-32
}
