package tms.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import tms.domain.enums.StatusCamion;
import tms.domain.vo.NrInmatriculare;
import tms.persistence.converter.NrInmatriculareConverter;

/** Camion din flota companiei. Statusul tehnic este modificat doar prin servicii de workflow. */
@Entity
@Table(name = "camion", uniqueConstraints = @UniqueConstraint(name = "uk_camion_nr", columnNames = "nr_inmatriculare"))
public class Camion extends AbstractEntity {

    @NotNull(message = "RB-06: Numărul de înmatriculare este obligatoriu")
    @Valid
    @Convert(converter = NrInmatriculareConverter.class)
    @Column(name = "nr_inmatriculare", nullable = false, length = 10)
    private NrInmatriculare nrInmatriculare;

    @NotBlank(message = "RB-07: Marca este obligatorie")
    @Column(name = "marca", length = 60)
    private String marca;

    @NotBlank(message = "RB-07: Modelul este obligatoriu")
    @Column(name = "model", length = 60)
    private String model;

    @Min(value = 1990, message = "RB-07: Anul fabricației trebuie să fie cel puțin 1990")
    @Column(name = "an_fabricatie")
    private Integer anFabricatie;

    @NotNull(message = "RB-07: Capacitatea este obligatorie")
    @Positive(message = "RB-07: Capacitatea trebuie să fie pozitivă")
    @Column(name = "capacitate_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacitateKg;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status_tehnic", nullable = false, length = 15)
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
