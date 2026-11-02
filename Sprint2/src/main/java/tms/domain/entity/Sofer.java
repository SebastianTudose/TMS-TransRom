package tms.domain.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import tms.domain.enums.Rol;

/** Utilizator cu rolul SOFER; resursă de transport cu permis de conducere. */
public class Sofer extends Utilizator {

    @NotBlank(message = "RB-08: Seria permisului este obligatorie")
    @Pattern(regexp = "^[A-Z0-9]{5,15}$", message = "RB-08: Seria permisului conține caractere invalide")
    private String seriePermis;

    @NotNull(message = "RB-08: Data expirării permisului este obligatorie")
    private LocalDate dataExpirarePermis;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "RB-08: Număr de telefon invalid")
    private String telefon;

    public String getSeriePermis() { return seriePermis; }
    public void setSeriePermis(String seriePermis) { this.seriePermis = seriePermis; }

    public LocalDate getDataExpirarePermis() { return dataExpirarePermis; }
    public void setDataExpirarePermis(LocalDate dataExpirarePermis) { this.dataExpirarePermis = dataExpirarePermis; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    @Override
    public Rol getRol() { return Rol.SOFER; }

}
