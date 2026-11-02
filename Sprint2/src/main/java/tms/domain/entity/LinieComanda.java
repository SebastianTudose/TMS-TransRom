package tms.domain.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Componentă a agregatului ComandaTransport: o linie de marfă. Greutatea și volumul total sunt câmpuri calculate (RB-44). */
public class LinieComanda extends AbstractEntity {

    @NotBlank(message = "RB-12: Descrierea mărfii este obligatorie")
    private String descriereMarfa;

    @NotNull(message = "RB-12: Cantitatea este obligatorie")
    @Positive(message = "RB-12: Cantitatea trebuie să fie pozitivă")
    private Integer cantitate;

    @NotNull(message = "RB-12: Greutatea unitară este obligatorie")
    @Positive(message = "RB-12: Greutatea unitară trebuie să fie pozitivă")
    private BigDecimal greutateUnitaraKg;

    @NotNull(message = "RB-12: Volumul unitar este obligatoriu")
    @Positive(message = "RB-12: Volumul unitar trebuie să fie pozitiv")
    private BigDecimal volumUnitarM3;

    private BigDecimal greutateTotalaKg = BigDecimal.ZERO;

    private BigDecimal volumTotalM3 = BigDecimal.ZERO;

    private ComandaTransport comanda;

    public String getDescriereMarfa() { return descriereMarfa; }
    public void setDescriereMarfa(String descriereMarfa) { this.descriereMarfa = descriereMarfa; }

    public Integer getCantitate() { return cantitate; }
    public void setCantitate(Integer cantitate) { this.cantitate = cantitate; }

    public BigDecimal getGreutateUnitaraKg() { return greutateUnitaraKg; }
    public void setGreutateUnitaraKg(BigDecimal greutateUnitaraKg) { this.greutateUnitaraKg = greutateUnitaraKg; }

    public BigDecimal getVolumUnitarM3() { return volumUnitarM3; }
    public void setVolumUnitarM3(BigDecimal volumUnitarM3) { this.volumUnitarM3 = volumUnitarM3; }

    public BigDecimal getGreutateTotalaKg() { return greutateTotalaKg; }
    public void setGreutateTotalaKg(BigDecimal greutateTotalaKg) { this.greutateTotalaKg = greutateTotalaKg; }

    public BigDecimal getVolumTotalM3() { return volumTotalM3; }
    public void setVolumTotalM3(BigDecimal volumTotalM3) { this.volumTotalM3 = volumTotalM3; }

    public ComandaTransport getComanda() { return comanda; }
    public void setComanda(ComandaTransport comanda) { this.comanda = comanda; }

}
