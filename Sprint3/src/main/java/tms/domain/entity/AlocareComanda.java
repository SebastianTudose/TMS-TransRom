package tms.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Componentă a agregatului Ruta: alocarea unei comenzi pe cursă (grupaj). */
@Entity
@Table(name = "alocare_comanda",
        uniqueConstraints = @UniqueConstraint(name = "uk_alocare_ruta_comanda", columnNames = {"ruta_id", "comanda_id"}))
public class AlocareComanda extends AbstractEntity {

    @NotNull(message = "RB-25: Comanda alocată este obligatorie")
    @Column(name = "comanda_id", nullable = false)
    private Long comandaId;

    @NotNull
    @Positive(message = "RB-19: Greutatea alocată trebuie să fie pozitivă")
    @Column(name = "greutate_kg", nullable = false, precision = 14, scale = 3)
    private BigDecimal greutateKg;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    public Long getComandaId() { return comandaId; }
    public void setComandaId(Long comandaId) { this.comandaId = comandaId; }

    public BigDecimal getGreutateKg() { return greutateKg; }
    public void setGreutateKg(BigDecimal greutateKg) { this.greutateKg = greutateKg; }

    public Ruta getRuta() { return ruta; }
    public void setRuta(Ruta ruta) { this.ruta = ruta; }

}
