package tms.domain.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Obiect valoare: număr de înmatriculare, fără spații (ex. IS12ABC, B123ABC, ABC123). */
public record NrInmatriculare(
        @NotBlank(message = "RB-06: Numărul de înmatriculare este obligatoriu")
        @Pattern(regexp = "^([A-Z]{1,2}[0-9]{2,3}[A-Z]{3}|[A-Z]{3}[0-9]{3})$", message = "RB-06: Număr de înmatriculare invalid")
        String valoare) {
}
