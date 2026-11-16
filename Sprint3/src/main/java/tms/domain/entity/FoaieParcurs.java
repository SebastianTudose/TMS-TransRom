package tms.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import tms.domain.enums.StatusDocument;
import tms.persistence.listener.NoDeleteListener;

/** Componentă a agregatului Ruta: foaia de parcurs (date de bord). */
@Entity
@Table(name = "foaie_parcurs")
@EntityListeners(NoDeleteListener.class)
public class FoaieParcurs extends AbstractEntity {

    @Column(name = "data_emiterii")
    private LocalDateTime dataEmiterii;

    @NotNull(message = "RB-13: Kilometrajul de plecare este obligatoriu")
    @PositiveOrZero(message = "RB-13: Kilometrajul de plecare nu poate fi negativ")
    @Column(name = "km_plecare", precision = 10, scale = 2)
    private BigDecimal kmPlecare;

    @PositiveOrZero(message = "RB-13: Kilometrajul de sosire nu poate fi negativ")
    @Column(name = "km_sosire", precision = 10, scale = 2)
    private BigDecimal kmSosire;

    @PositiveOrZero(message = "RB-13: Consumul declarat nu poate fi negativ")
    @Column(name = "consum_carburant", precision = 10, scale = 2)
    private BigDecimal consumCarburant;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private StatusDocument status = StatusDocument.INTOCMIT;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ruta_id", nullable = false, unique = true)
    private Ruta ruta;

    public LocalDateTime getDataEmiterii() { return dataEmiterii; }
    public void setDataEmiterii(LocalDateTime dataEmiterii) { this.dataEmiterii = dataEmiterii; }

    public BigDecimal getKmPlecare() { return kmPlecare; }
    public void setKmPlecare(BigDecimal kmPlecare) { this.kmPlecare = kmPlecare; }

    public BigDecimal getKmSosire() { return kmSosire; }
    public void setKmSosire(BigDecimal kmSosire) { this.kmSosire = kmSosire; }

    public BigDecimal getConsumCarburant() { return consumCarburant; }
    public void setConsumCarburant(BigDecimal consumCarburant) { this.consumCarburant = consumCarburant; }

    public StatusDocument getStatus() { return status; }
    public void setStatus(StatusDocument status) { this.status = status; }

    public Ruta getRuta() { return ruta; }
    public void setRuta(Ruta ruta) { this.ruta = ruta; }


    @AssertTrue(message = "RB-13: Kilometrajul de sosire trebuie să fie cel puțin egal cu cel de plecare")
    public boolean isKmSosireValid() {
        return kmSosire == null || kmPlecare == null || kmSosire.compareTo(kmPlecare) >= 0;
    }

}
