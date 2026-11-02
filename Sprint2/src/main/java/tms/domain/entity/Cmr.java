package tms.domain.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import tms.domain.enums.StatusCmr;

/** Rădăcina agregatului Cmr: documentul de transport internațional. Nu se șterge, se anulează (RB-27). */
public class Cmr extends AbstractEntity {

    private String numarCmr;

    @NotNull(message = "RB-26: Ruta este obligatorie")
    private Long rutaId;

    @NotNull(message = "RB-26: Comanda este obligatorie")
    private Long comandaId;

    @NotNull(message = "RB-15: Greutatea brută este obligatorie")
    @Positive(message = "RB-15: Greutatea brută trebuie să fie pozitivă")
    private BigDecimal greutateBruta;

    private LocalDateTime dataEmiterii;

    @NotNull
    private StatusCmr status = StatusCmr.EMIS;

    public String getNumarCmr() { return numarCmr; }
    public void setNumarCmr(String numarCmr) { this.numarCmr = numarCmr; }

    public Long getRutaId() { return rutaId; }
    public void setRutaId(Long rutaId) { this.rutaId = rutaId; }

    public Long getComandaId() { return comandaId; }
    public void setComandaId(Long comandaId) { this.comandaId = comandaId; }

    public BigDecimal getGreutateBruta() { return greutateBruta; }
    public void setGreutateBruta(BigDecimal greutateBruta) { this.greutateBruta = greutateBruta; }

    public LocalDateTime getDataEmiterii() { return dataEmiterii; }
    public void setDataEmiterii(LocalDateTime dataEmiterii) { this.dataEmiterii = dataEmiterii; }

    public StatusCmr getStatus() { return status; }
    public void setStatus(StatusCmr status) { this.status = status; }

}
