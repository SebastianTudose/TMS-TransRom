package tms.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import tms.domain.enums.StatusCmr;
import tms.persistence.listener.NoDeleteListener;

/** Rădăcina agregatului Cmr: documentul de transport internațional. Nu se șterge, se anulează (RB-27). */
@Entity
@Table(name = "cmr",
        uniqueConstraints = @UniqueConstraint(name = "uk_cmr_numar", columnNames = "numar_cmr"),
        indexes = {@Index(name = "ix_cmr_ruta", columnList = "ruta_id"), @Index(name = "ix_cmr_comanda", columnList = "comanda_id")})
@EntityListeners(NoDeleteListener.class)
public class Cmr extends AbstractEntity {

    @Column(name = "numar_cmr", length = 20)
    private String numarCmr;

    @NotNull(message = "RB-26: Ruta este obligatorie")
    @Column(name = "ruta_id", nullable = false)
    private Long rutaId;

    @NotNull(message = "RB-26: Comanda este obligatorie")
    @Column(name = "comanda_id", nullable = false)
    private Long comandaId;

    @NotNull(message = "RB-15: Greutatea brută este obligatorie")
    @Positive(message = "RB-15: Greutatea brută trebuie să fie pozitivă")
    @Column(name = "greutate_bruta", nullable = false, precision = 14, scale = 3)
    private BigDecimal greutateBruta;

    @Column(name = "data_emiterii")
    private LocalDateTime dataEmiterii;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
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
