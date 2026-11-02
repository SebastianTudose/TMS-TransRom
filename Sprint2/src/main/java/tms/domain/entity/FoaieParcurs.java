package tms.domain.entity;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import tms.domain.enums.StatusDocument;

/** Componentă a agregatului Ruta: foaia de parcurs (date de bord). */
public class FoaieParcurs extends AbstractEntity {

    private LocalDateTime dataEmiterii;

    @NotNull(message = "RB-13: Kilometrajul de plecare este obligatoriu")
    @PositiveOrZero(message = "RB-13: Kilometrajul de plecare nu poate fi negativ")
    private BigDecimal kmPlecare;

    @PositiveOrZero(message = "RB-13: Kilometrajul de sosire nu poate fi negativ")
    private BigDecimal kmSosire;

    @PositiveOrZero(message = "RB-13: Consumul declarat nu poate fi negativ")
    private BigDecimal consumCarburant;

    @NotNull
    private StatusDocument status = StatusDocument.INTOCMIT;

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
