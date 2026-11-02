package tms.domain.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import tms.domain.vo.SumaMonetara;

/** Componentă a agregatului Factura: o comandă facturată. */
public class LinieFactura extends AbstractEntity {

    @NotNull(message = "RB-35: Comanda facturată este obligatorie")
    private Long comandaId;

    @NotNull(message = "RB-17: Valoarea liniei este obligatorie")
    @Valid
    private SumaMonetara valoare;

    private Factura factura;

    public Long getComandaId() { return comandaId; }
    public void setComandaId(Long comandaId) { this.comandaId = comandaId; }

    public SumaMonetara getValoare() { return valoare; }
    public void setValoare(SumaMonetara valoare) { this.valoare = valoare; }

    public Factura getFactura() { return factura; }
    public void setFactura(Factura factura) { this.factura = factura; }

}
