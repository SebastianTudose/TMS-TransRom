package tms.domain.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import tms.domain.enums.Moneda;
import tms.domain.enums.StatusFactura;
import tms.domain.vo.SumaMonetara;

/** Rădăcina agregatului Factura; componente: LinieFactura. valoareTotala este câmp calculat (RB-45). */
public class Factura extends AbstractEntity {

    private String numar;

    @NotNull(message = "RB-17: Clientul este obligatoriu")
    private Long clientId;

    private LocalDate dataEmiterii;

    @NotNull(message = "RB-17: Data scadenței este obligatorie")
    private LocalDate dataScadenta;

    @NotNull(message = "RB-17: Moneda facturii este obligatorie")
    private Moneda moneda;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal discountAplicat = BigDecimal.ZERO;

    @Valid
    private SumaMonetara valoareTotala;

    @NotNull
    private StatusFactura status = StatusFactura.EMISA;

    @Size(min = 1, message = "RB-17: Factura trebuie să aibă cel puțin o linie")
    @Valid
    private List<LinieFactura> linii = new ArrayList<>();

    public String getNumar() { return numar; }
    public void setNumar(String numar) { this.numar = numar; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public LocalDate getDataEmiterii() { return dataEmiterii; }
    public void setDataEmiterii(LocalDate dataEmiterii) { this.dataEmiterii = dataEmiterii; }

    public LocalDate getDataScadenta() { return dataScadenta; }
    public void setDataScadenta(LocalDate dataScadenta) { this.dataScadenta = dataScadenta; }

    public Moneda getMoneda() { return moneda; }
    public void setMoneda(Moneda moneda) { this.moneda = moneda; }

    public BigDecimal getDiscountAplicat() { return discountAplicat; }
    public void setDiscountAplicat(BigDecimal discountAplicat) { this.discountAplicat = discountAplicat; }

    public SumaMonetara getValoareTotala() { return valoareTotala; }
    public void setValoareTotala(SumaMonetara valoareTotala) { this.valoareTotala = valoareTotala; }

    public StatusFactura getStatus() { return status; }
    public void setStatus(StatusFactura status) { this.status = status; }


    public List<LinieFactura> getLinii() { return Collections.unmodifiableList(linii); }

    public void addLinie(LinieFactura linie) { linii.add(linie); linie.setFactura(this); }

    @AssertTrue(message = "RB-17: Data scadenței trebuie să fie cel puțin egală cu data emiterii")
    public boolean isScadentaValida() {
        return dataEmiterii == null || dataScadenta == null || !dataScadenta.isBefore(dataEmiterii);
    }

}
