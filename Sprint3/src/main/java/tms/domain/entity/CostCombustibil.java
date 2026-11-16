package tms.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import tms.domain.vo.SumaMonetara;
import tms.persistence.listener.CostCombustibilCalculListener;

/** Componentă a agregatului Ruta: costul de combustibil. costTotal este câmp calculat (RB-43). */
@Entity
@Table(name = "cost_combustibil")
@EntityListeners(CostCombustibilCalculListener.class)
public class CostCombustibil extends AbstractEntity {

    @Column(name = "data_calcul")
    private LocalDateTime dataCalcul;

    @NotNull(message = "RB-14: Consumul total este obligatoriu")
    @PositiveOrZero(message = "RB-14: Consumul total nu poate fi negativ")
    @Column(name = "consum_total", precision = 10, scale = 2)
    private BigDecimal consumTotal;

    @NotNull(message = "RB-14: Prețul per litru este obligatoriu")
    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valoare", column = @Column(name = "pret_valoare", precision = 14, scale = 2)),
            @AttributeOverride(name = "moneda", column = @Column(name = "pret_moneda", length = 3))
    })
    private SumaMonetara pretPerLitru;

    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valoare", column = @Column(name = "cost_valoare", precision = 14, scale = 2)),
            @AttributeOverride(name = "moneda", column = @Column(name = "cost_moneda", length = 3))
    })
    private SumaMonetara costTotal;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ruta_id", nullable = false, unique = true)
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
