package tms.domain.vo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Obiect valoare: loc de încărcare / descărcare / plecare / destinație. */
@Embeddable
public record Locatie(
        @NotBlank(message = "RB-11: Țara este obligatorie")
        @Pattern(regexp = "^[A-Z]{2}$", message = "RB-11: Țara trebuie dată prin cod ISO de 2 litere")
        String tara,
        @NotBlank(message = "RB-11: Localitatea este obligatorie")
        @Pattern(regexp = "^\\p{L}[\\p{L} .'\\-]*$", message = "RB-11: Localitatea conține caractere invalide")
        String localitate,
        String adresa) {
}
