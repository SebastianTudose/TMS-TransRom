package tms.domain.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import tms.domain.enums.StatusCamion;
import tms.domain.vo.NrInmatriculare;

/** Camion din flota companiei. Statusul tehnic este modificat doar prin servicii de workflow. */
public class Camion extends AbstractEntity {

    @NotNull(message = "RB-06: Numărul de înmatriculare este obligatoriu")
    @Valid
    private NrInmatriculare nrInmatriculare;

    @NotBlank(message = "RB-07: Marca este obligatorie")
    private String marca;

    @NotBlank(message = "RB-07: Modelul este obligatoriu")
    private String model;

    @Min(value = 1990, message = "RB-07: Anul fabricației trebuie să fie cel puțin 1990")
    private Integer anFabricatie;

    @NotNull(message = "RB-07: Capacitatea este obligatorie")
    @Positive(message = "RB-07: Capacitatea trebuie să fie pozitivă")
    private BigDecimal capacitateKg;

    @NotNull
    private StatusCamion statusTehnic = StatusCamion.DISPONIBIL;

    public NrInmatriculare getNrInmatriculare() { return nrInmatriculare; }
    public void setNrInmatriculare(NrInmatriculare nrInmatriculare) { this.nrInmatriculare = nrInmatriculare; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getAnFabricatie() { return anFabricatie; }
    public void setAnFabricatie(Integer anFabricatie) { this.anFabricatie = anFabricatie; }

    public BigDecimal getCapacitateKg() { return capacitateKg; }
    public void setCapacitateKg(BigDecimal capacitateKg) { this.capacitateKg = capacitateKg; }

    public StatusCamion getStatusTehnic() { return statusTehnic; }
    public void setStatusTehnic(StatusCamion statusTehnic) { this.statusTehnic = statusTehnic; }

}
