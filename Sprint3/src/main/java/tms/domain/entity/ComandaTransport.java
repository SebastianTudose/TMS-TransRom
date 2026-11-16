package tms.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import tms.domain.enums.StatusComanda;
import tms.domain.vo.Locatie;
import tms.domain.vo.SumaMonetara;

/** Rădăcina agregatului ComandaTransport; componente: LinieComanda. */
@Entity
@Table(name = "comanda_transport",
        uniqueConstraints = @UniqueConstraint(name = "uk_comanda_nr", columnNames = "nr_comanda"),
        indexes = {@Index(name = "ix_comanda_client", columnList = "client_id"), @Index(name = "ix_comanda_status", columnList = "status")})
public class ComandaTransport extends AbstractEntity {

    @Column(name = "nr_comanda", length = 20)
    private String nrComanda;

    @NotNull(message = "RB-16: Clientul este obligatoriu")
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "data_comanda")
    private LocalDate dataComanda;

    @Column(name = "data_limita_livrare")
    private LocalDate dataLimitaLivrare;

    @NotNull(message = "RB-11: Locația de încărcare este obligatorie")
    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "tara", column = @Column(name = "incarcare_tara", length = 2)),
            @AttributeOverride(name = "localitate", column = @Column(name = "incarcare_localitate", length = 120)),
            @AttributeOverride(name = "adresa", column = @Column(name = "incarcare_adresa", length = 200))
    })
    private Locatie locatieIncarcare;

    @NotNull(message = "RB-11: Locația de descărcare este obligatorie")
    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "tara", column = @Column(name = "descarcare_tara", length = 2)),
            @AttributeOverride(name = "localitate", column = @Column(name = "descarcare_localitate", length = 120)),
            @AttributeOverride(name = "adresa", column = @Column(name = "descarcare_adresa", length = 200))
    })
    private Locatie locatieDescarcare;

    @NotNull(message = "RB-16: Tariful agreat este obligatoriu")
    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valoare", column = @Column(name = "tarif_valoare", precision = 14, scale = 2)),
            @AttributeOverride(name = "moneda", column = @Column(name = "tarif_moneda", length = 3))
    })
    private SumaMonetara tarifAgreat;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private StatusComanda status = StatusComanda.NOUA;

    @Size(min = 1, message = "RB-16: Comanda trebuie să aibă cel puțin o linie de marfă")
    @Valid
    @OneToMany(mappedBy = "comanda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<LinieComanda> linii = new ArrayList<>();

    public String getNrComanda() { return nrComanda; }
    public void setNrComanda(String nrComanda) { this.nrComanda = nrComanda; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public LocalDate getDataComanda() { return dataComanda; }
    public void setDataComanda(LocalDate dataComanda) { this.dataComanda = dataComanda; }

    public LocalDate getDataLimitaLivrare() { return dataLimitaLivrare; }
    public void setDataLimitaLivrare(LocalDate dataLimitaLivrare) { this.dataLimitaLivrare = dataLimitaLivrare; }

    public Locatie getLocatieIncarcare() { return locatieIncarcare; }
    public void setLocatieIncarcare(Locatie locatieIncarcare) { this.locatieIncarcare = locatieIncarcare; }

    public Locatie getLocatieDescarcare() { return locatieDescarcare; }
    public void setLocatieDescarcare(Locatie locatieDescarcare) { this.locatieDescarcare = locatieDescarcare; }

    public SumaMonetara getTarifAgreat() { return tarifAgreat; }
    public void setTarifAgreat(SumaMonetara tarifAgreat) { this.tarifAgreat = tarifAgreat; }

    public StatusComanda getStatus() { return status; }
    public void setStatus(StatusComanda status) { this.status = status; }


    public List<LinieComanda> getLinii() { return Collections.unmodifiableList(linii); }

    public void addLinie(LinieComanda linie) { linii.add(linie); linie.setComanda(this); }

    public void removeLinie(LinieComanda linie) { linii.remove(linie); linie.setComanda(null); }

}
