package tms.domain.entity;

import jakarta.persistence.*;
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
import tms.persistence.converter.AdresaEmailConverter;
import tms.persistence.converter.IbanConverter;

/** Client al companiei de transport. Soldul debitor este exprimat în moneda de facturare. */
@Entity
@Table(name = "client", uniqueConstraints = @UniqueConstraint(name = "uk_client_cod_fiscal", columnNames = "cod_fiscal"))
public class Client extends AbstractEntity {

    @NotBlank(message = "RB-03: Denumirea clientului este obligatorie")
    @Column(name = "denumire", nullable = false, length = 200)
    private String denumire;

    @NotBlank(message = "RB-03: Codul fiscal este obligatoriu")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2,12}$", message = "RB-03: Cod fiscal invalid (prefix național urmat de cifre)")
    @Column(name = "cod_fiscal", nullable = false, length = 14)
    private String codFiscal;

    @NotNull(message = "RB-04: IBAN-ul este obligatoriu")
    @Valid
    @Convert(converter = IbanConverter.class)
    @Column(name = "cont_bancar", nullable = false, length = 24)
    private Iban contBancar;

    @Valid
    @Convert(converter = AdresaEmailConverter.class)
    @Column(name = "email", length = 254)
    private AdresaEmail email;

    @Column(name = "telefon", length = 20)
    private String telefon;

    @NotNull(message = "RB-05: Discountul este obligatoriu")
    @DecimalMin(value = "0", message = "RB-05: Discountul nu poate fi negativ")
    @DecimalMax(value = "100", message = "RB-05: Discountul nu poate depăși 100%")
    @Column(name = "discount_acordat", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountAcordat = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "moneda_facturare", nullable = false, length = 3)
    private Moneda monedaFacturare = Moneda.EUR;

    @NotNull
    @PositiveOrZero(message = "Soldul debitor nu poate fi negativ")
    @Column(name = "sold_debitor", nullable = false, precision = 14, scale = 2)
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
