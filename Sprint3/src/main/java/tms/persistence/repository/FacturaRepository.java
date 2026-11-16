package tms.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tms.domain.entity.Factura;
import tms.domain.enums.StatusFactura;

/** Repository pentru rădăcina agregatului Factura (liniile se salvează prin cascadă). */
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    Optional<Factura> findByNumar(String numar);

    List<Factura> findByClientId(Long clientId);

    List<Factura> findByClientIdAndStatus(Long clientId, StatusFactura status);

    /** Facturile cu statusul dat și scadența anterioară datei (de regulă EMISA și restante). */
    @Query("select f from Factura f where f.status = :status and f.dataScadenta < :data order by f.dataScadenta")
    List<Factura> findScadente(@Param("status") StatusFactura status, @Param("data") LocalDate data);

    /** Factura care conține comanda dată (o comandă poate fi facturată o singură dată – RB-35). */
    @Query("select f from Factura f join f.linii l where l.comandaId = :comandaId")
    List<Factura> findByComandaId(@Param("comandaId") Long comandaId);

    @Query("select distinct f from Factura f left join fetch f.linii where f.id = :id")
    Optional<Factura> findCuLinii(@Param("id") Long id);
}
