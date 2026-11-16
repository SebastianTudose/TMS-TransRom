package tms.persistence.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tms.domain.entity.Ruta;
import tms.domain.enums.StatusRuta;

/** Repository pentru rădăcina agregatului Ruta (foaia de parcurs, costul și alocările se salvează prin cascadă). */
public interface RutaRepository extends JpaRepository<Ruta, Long> {

    Optional<Ruta> findByCodRuta(String codRuta);

    List<Ruta> findByStatus(StatusRuta status);

    /** Cursele unui șofer, în ordinea plecării planificate (UC-10). */
    List<Ruta> findBySoferIdOrderByIntervalPlanificatInceputAsc(Long soferId);

    /**
     * Cursele cu unul dintre statusurile date, care folosesc același camion sau același șofer și al căror interval
     * planificat se suprapune cu [început, sfârșit) – baza regulii RB-22.
     */
    @Query("select r from Ruta r where r.status in :statusuri and (r.camionId = :camionId or r.soferId = :soferId) "
            + "and r.intervalPlanificat.inceput < :sfarsit and r.intervalPlanificat.sfarsit > :inceput")
    List<Ruta> findSuprapuse(@Param("statusuri") Collection<StatusRuta> statusuri,
                             @Param("camionId") Long camionId, @Param("soferId") Long soferId,
                             @Param("inceput") LocalDateTime inceput, @Param("sfarsit") LocalDateTime sfarsit);

    /** Cursele cu statusul dat al căror cost de combustibil a fost calculat în perioadă (baza raportului RB-48). */
    @Query("select r from Ruta r join r.costCombustibil c where r.status = :status "
            + "and c.dataCalcul between :de and :la order by c.dataCalcul")
    List<Ruta> findCuCostInPerioada(@Param("status") StatusRuta status,
                                    @Param("de") LocalDateTime de, @Param("la") LocalDateTime la);
}
