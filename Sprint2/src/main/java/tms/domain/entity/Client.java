package tms.domain.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import tms.domain.enums.Moneda;
import tms.domain.vo.AdresaEmail;
import tms.domain.vo.Iban;

/** Client al companiei de transport. Soldul debitor este exprimat în moneda de facturare. */
public class Client extends AbstractEntity {

    @NotBlank(message = "RB-03: Denumirea clientului este obligatorie")
    private String denumire;

    @NotBlank(message = "RB-03: Codul fiscal este obligatoriu")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2,12}$", message = "RB-03: Cod fiscal invalid (prefix național urmat de cifre)")
    private String codFiscal;

    @NotNull(message = "RB-04: IBAN-ul este obligatoriu")
    @Valid
    private Iban contBancar;

    @Valid
    private AdresaEmail email;

    private String telefon;

    @NotNull(message = "RB-05: Discountul este obligatoriu")
    @DecimalMin(value = "0", message = "RB-05: Discountul nu poate fi negativ")
    @DecimalMax(value = "100", message = "RB-05: Discountul nu poate depăși 100%")
    private BigDecimal discountAcordat = BigDecimal.ZERO;

    @NotNull
    private Moneda monedaFacturare = Moneda.EUR;

    @NotNull
    @PositiveOrZero(message = "Soldul debitor nu poate fi negativ")
    private BigDecimal soldDebitor = BigDecimal.ZERO;

    public String getDenumire() { return denumire; }
    public void setDenumire(String denumire) { this.denumire = denumire; }

    public String getCodFiscal() { return codFiscal; }
    public void setCodFiscal(String codFiscal) { this.codFiscal = codFiscal; }

    public Iban getContBancar() { return contBancar; }
    public void setContBancar(Iban contBancar) { this.contBancar = contBancar; }

    public AdresaEmail getEmail() { return email; }
    public void setEmail(AdresaEmail email) { this.email = email; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public BigDecimal getDiscountAcordat() { return discountAcordat; }
    public void setDiscountAcordat(BigDecimal discountAcordat) { this.discountAcordat = discountAcordat; }

    public Moneda getMonedaFacturare() { return monedaFacturare; }
    public void setMonedaFacturare(Moneda monedaFacturare) { this.monedaFacturare = monedaFacturare; }

    public BigDecimal getSoldDebitor() { return soldDebitor; }
    public void setSoldDebitor(BigDecimal soldDebitor) { this.soldDebitor = soldDebitor; }

}
