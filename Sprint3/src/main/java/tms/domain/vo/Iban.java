package tms.domain.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Obiect valoare: IBAN (24 de caractere). */
public record Iban(
        @NotBlank(message = "RB-04: IBAN-ul este obligatoriu")
        @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{20}$", message = "RB-04: IBAN invalid (24 de caractere: 2 litere, 2 cifre, 20 alfanumerice)")
        String valoare) {
}
