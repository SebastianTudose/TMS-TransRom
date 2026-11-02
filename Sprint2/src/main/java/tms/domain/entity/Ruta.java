package tms.domain.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import tms.domain.enums.StatusRuta;
import tms.domain.vo.IntervalTimp;
import tms.domain.vo.Locatie;

/** Rădăcina agregatului Ruta (cursă); componente: FoaieParcurs, CostCombustibil, AlocareComanda. */
public class Ruta extends AbstractEntity {

    private String codRuta;

    @NotNull(message = "RB-18: Locația de plecare este obligatorie")
    @Valid
    private Locatie locatiePlecare;

    @NotNull(message = "RB-18: Destinația este obligatorie")
    @Valid
    private Locatie locatieDestinatie;

    @NotNull(message = "RB-18: Intervalul planificat este obligatoriu")
    @Valid
    private IntervalTimp intervalPlanificat;

    private LocalDateTime dataSosireReala;

    @NotNull
    @PositiveOrZero
    private BigDecimal distantaParcursa = BigDecimal.ZERO;

    @NotNull
    private StatusRuta status = StatusRuta.PLANIFICATA;

    @NotNull(message = "RB-18: Camionul este obligatoriu")
    private Long camionId;

    @NotNull(message = "RB-18: Șoferul este obligatoriu")
    private Long soferId;

    @Valid
    private FoaieParcurs foaieParcurs;

    @Valid
    private CostCombustibil costCombustibil;

    @Valid
    private List<AlocareComanda> alocari = new ArrayList<>();

    public String getCodRuta() { return codRuta; }
    public void setCodRuta(String codRuta) { this.codRuta = codRuta; }

    public Locatie getLocatiePlecare() { return locatiePlecare; }
    public void setLocatiePlecare(Locatie locatiePlecare) { this.locatiePlecare = locatiePlecare; }

    public Locatie getLocatieDestinatie() { return locatieDestinatie; }
    public void setLocatieDestinatie(Locatie locatieDestinatie) { this.locatieDestinatie = locatieDestinatie; }

    public IntervalTimp getIntervalPlanificat() { return intervalPlanificat; }
    public void setIntervalPlanificat(IntervalTimp intervalPlanificat) { this.intervalPlanificat = intervalPlanificat; }

    public LocalDateTime getDataSosireReala() { return dataSosireReala; }
    public void setDataSosireReala(LocalDateTime dataSosireReala) { this.dataSosireReala = dataSosireReala; }

    public BigDecimal getDistantaParcursa() { return distantaParcursa; }
    public void setDistantaParcursa(BigDecimal distantaParcursa) { this.distantaParcursa = distantaParcursa; }

    public StatusRuta getStatus() { return status; }
    public void setStatus(StatusRuta status) { this.status = status; }

    public Long getCamionId() { return camionId; }
    public void setCamionId(Long camionId) { this.camionId = camionId; }

    public Long getSoferId() { return soferId; }
    public void setSoferId(Long soferId) { this.soferId = soferId; }


    public FoaieParcurs getFoaieParcurs() { return foaieParcurs; }

    public void setFoaieParcurs(FoaieParcurs foaie) {
        this.foaieParcurs = foaie;
        if (foaie != null) foaie.setRuta(this);
    }

    public CostCombustibil getCostCombustibil() { return costCombustibil; }

    public void setCostCombustibil(CostCombustibil cost) {
        this.costCombustibil = cost;
        if (cost != null) cost.setRuta(this);
    }

    public List<AlocareComanda> getAlocari() { return Collections.unmodifiableList(alocari); }

    public void addAlocare(AlocareComanda alocare) { alocari.add(alocare); alocare.setRuta(this); }

    public void removeAlocare(AlocareComanda alocare) { alocari.remove(alocare); alocare.setRuta(null); }

}
