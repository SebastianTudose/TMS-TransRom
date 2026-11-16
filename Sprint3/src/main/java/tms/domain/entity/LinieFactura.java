package tms.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import tms.domain.vo.SumaMonetara;

/** Componentă a agregatului Factura: o comandă facturată. */
@Entity
@Table(name = "linie_factura")
public class LinieFactura extends AbstractEntity {

    @NotNull(message = "RB-35: Comanda facturată este obligatorie")
    @Column(name = "comanda_id", nullable = false)
    private Long comandaId;

    @NotNull(message = "RB-17: Valoarea liniei este obligatorie")
    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valoare", column = @Column(name = "linie_valoare", precision = 14, scale = 2)),
            @AttributeOverride(name = "moneda", column = @Column(name = "linie_moneda", length = 3))
    })
    private SumaMonetara valoare;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    public Long getComandaId() { return comandaId; }
    public void setComandaId(Long comandaId) { this.comandaId = comandaId; }

    public SumaMonetara getValoare() { return valoare; }
    public void setValoare(SumaMonetara valoare) { this.valoare = valoare; }

    public Factura getFactura() { return factura; }
    public void setFactura(Factura factura) { this.factura = factura; }

}
