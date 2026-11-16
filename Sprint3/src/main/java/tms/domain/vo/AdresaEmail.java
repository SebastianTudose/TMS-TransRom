package tms.domain.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Obiect valoare: adresă de email. */
public record AdresaEmail(
        @NotBlank(message = "RB-01: Adresa de email este obligatorie")
        @Email(message = "RB-01: Format de email invalid")
        String valoare) {
}
