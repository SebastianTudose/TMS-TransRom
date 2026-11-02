package tms.domain.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Componentă a agregatului Ruta: alocarea unei comenzi pe cursă (grupaj). */
public class AlocareComanda extends AbstractEntity {

    @NotNull(message = "RB-25: Comanda alocată este obligatorie")
    private Long comandaId;

    @NotNull
    @Positive(message = "RB-19: Greutatea alocată trebuie să fie pozitivă")
    private BigDecimal greutateKg;

    private Ruta ruta;

    public Long getComandaId() { return comandaId; }
    public void setComandaId(Long comandaId) { this.comandaId = comandaId; }

    public BigDecimal getGreutateKg() { return greutateKg; }
    public void setGreutateKg(BigDecimal greutateKg) { this.greutateKg = greutateKg; }

    public Ruta getRuta() { return ruta; }
    public void setRuta(Ruta ruta) { this.ruta = ruta; }

}
