package tms.domain.entity;

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
public class ComandaTransport extends AbstractEntity {

    private String nrComanda;

    @NotNull(message = "RB-16: Clientul este obligatoriu")
    private Long clientId;

    private LocalDate dataComanda;

    private LocalDate dataLimitaLivrare;

    @NotNull(message = "RB-11: Locația de încărcare este obligatorie")
    @Valid
    private Locatie locatieIncarcare;

    @NotNull(message = "RB-11: Locația de descărcare este obligatorie")
    @Valid
    private Locatie locatieDescarcare;

    @NotNull(message = "RB-16: Tariful agreat este obligatoriu")
    @Valid
    private SumaMonetara tarifAgreat;

    @NotNull
    private StatusComanda status = StatusComanda.NOUA;

    @Size(min = 1, message = "RB-16: Comanda trebuie să aibă cel puțin o linie de marfă")
    @Valid
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
