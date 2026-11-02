package tms.domain.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import tms.domain.vo.SumaMonetara;

/** Componentă a agregatului Ruta: costul de combustibil. costTotal este câmp calculat (RB-43). */
public class CostCombustibil extends AbstractEntity {

    private LocalDateTime dataCalcul;

    @NotNull(message = "RB-14: Consumul total este obligatoriu")
    @PositiveOrZero(message = "RB-14: Consumul total nu poate fi negativ")
    private BigDecimal consumTotal;

    @NotNull(message = "RB-14: Prețul per litru este obligatoriu")
    @Valid
    private SumaMonetara pretPerLitru;

    @Valid
    private SumaMonetara costTotal;

    private Ruta ruta;

    public LocalDateTime getDataCalcul() { return dataCalcul; }
    public void setDataCalcul(LocalDateTime dataCalcul) { this.dataCalcul = dataCalcul; }

    public BigDecimal getConsumTotal() { return consumTotal; }
    public void setConsumTotal(BigDecimal consumTotal) { this.consumTotal = consumTotal; }

    public SumaMonetara getPretPerLitru() { return pretPerLitru; }
    public void setPretPerLitru(SumaMonetara pretPerLitru) { this.pretPerLitru = pretPerLitru; }

    public SumaMonetara getCostTotal() { return costTotal; }
    public void setCostTotal(SumaMonetara costTotal) { this.costTotal = costTotal; }

    public Ruta getRuta() { return ruta; }
    public void setRuta(Ruta ruta) { this.ruta = ruta; }

}
