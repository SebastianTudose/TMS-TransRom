package tms.domain.entity;

import jakarta.persistence.*;
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
import tms.persistence.listener.NoDeleteListener;

/** Rădăcina agregatului Ruta (cursă); componente: FoaieParcurs, CostCombustibil, AlocareComanda. */
@Entity
@Table(name = "ruta",
        uniqueConstraints = @UniqueConstraint(name = "uk_ruta_cod", columnNames = "cod_ruta"),
        indexes = {@Index(name = "ix_ruta_camion", columnList = "camion_id"), @Index(name = "ix_ruta_sofer", columnList = "sofer_id"), @Index(name = "ix_ruta_status", columnList = "status")})
@EntityListeners(NoDeleteListener.class)
public class Ruta extends AbstractEntity {

    @Column(name = "cod_ruta", length = 20)
    private String codRuta;

    @NotNull(message = "RB-18: Locația de plecare este obligatorie")
    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "tara", column = @Column(name = "plecare_tara", length = 2)),
            @AttributeOverride(name = "localitate", column = @Column(name = "plecare_localitate", length = 120)),
            @AttributeOverride(name = "adresa", column = @Column(name = "plecare_adresa", length = 200))
    })
    private Locatie locatiePlecare;

    @NotNull(message = "RB-18: Destinația este obligatorie")
    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "tara", column = @Column(name = "destinatie_tara", length = 2)),
            @AttributeOverride(name = "localitate", column = @Column(name = "destinatie_localitate", length = 120)),
            @AttributeOverride(name = "adresa", column = @Column(name = "destinatie_adresa", length = 200))
    })
    private Locatie locatieDestinatie;

    @NotNull(message = "RB-18: Intervalul planificat este obligatoriu")
    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "inceput", column = @Column(name = "planificat_inceput")),
            @AttributeOverride(name = "sfarsit", column = @Column(name = "planificat_sfarsit"))
    })
    private IntervalTimp intervalPlanificat;

    @Column(name = "data_sosire_reala")
    private LocalDateTime dataSosireReala;

    @NotNull
    @PositiveOrZero
    @Column(name = "distanta_parcursa", nullable = false, precision = 10, scale = 2)
    private BigDecimal distantaParcursa = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private StatusRuta status = StatusRuta.PLANIFICATA;

    @NotNull(message = "RB-18: Camionul este obligatoriu")
    @Column(name = "camion_id", nullable = false)
    private Long camionId;

    @NotNull(message = "RB-18: Șoferul este obligatoriu")
    @Column(name = "sofer_id", nullable = false)
    private Long soferId;

    @Valid
    @OneToOne(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private FoaieParcurs foaieParcurs;

    @Valid
    @OneToOne(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CostCombustibil costCombustibil;

    @Valid
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
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
