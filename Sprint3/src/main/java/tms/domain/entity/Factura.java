package tms.domain.entity;

import jakarta.persistence.*;
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
import tms.persistence.listener.FacturaCalculListener;
import tms.persistence.listener.NoDeleteListener;

/** Rădăcina agregatului Factura; componente: LinieFactura. valoareTotala este câmp calculat (RB-45). */
@Entity
@Table(name = "factura",
        uniqueConstraints = @UniqueConstraint(name = "uk_factura_numar", columnNames = "numar"),
        indexes = {@Index(name = "ix_factura_client", columnList = "client_id"), @Index(name = "ix_factura_status", columnList = "status")})
@EntityListeners({NoDeleteListener.class, FacturaCalculListener.class})
public class Factura extends AbstractEntity {

    @Column(name = "numar", length = 20)
    private String numar;

    @NotNull(message = "RB-17: Clientul este obligatoriu")
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "data_emiterii")
    private LocalDate dataEmiterii;

    @NotNull(message = "RB-17: Data scadenței este obligatorie")
    @Column(name = "data_scadenta", nullable = false)
    private LocalDate dataScadenta;

    @NotNull(message = "RB-17: Moneda facturii este obligatorie")
    @Enumerated(EnumType.STRING)
    @Column(name = "moneda", nullable = false, length = 3)
    private Moneda moneda;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    @Column(name = "discount_aplicat", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountAplicat = BigDecimal.ZERO;

    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valoare", column = @Column(name = "total_valoare", precision = 14, scale = 2)),
            @AttributeOverride(name = "moneda", column = @Column(name = "total_moneda", length = 3))
    })
    private SumaMonetara valoareTotala;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private StatusFactura status = StatusFactura.EMISA;

    @Size(min = 1, message = "RB-17: Factura trebuie să aibă cel puțin o linie")
    @Valid
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
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
