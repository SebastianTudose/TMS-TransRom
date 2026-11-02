package tms.domain.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tms.domain.enums.Rol;
import tms.domain.vo.AdresaEmail;

/** Entitate abstractă: date comune de identificare și autentificare. Fiecare cont are un singur rol. */
public abstract class Utilizator extends AbstractEntity {

    @NotBlank(message = "RB-02: Numele este obligatoriu")
    private String nume;

    @NotBlank(message = "RB-02: Prenumele este obligatoriu")
    private String prenume;

    @NotNull(message = "RB-02: Adresa de email este obligatorie")
    @Valid
    private AdresaEmail email;

    @NotBlank(message = "RB-02: Datele de autentificare sunt obligatorii")
    private String hashParola;

    private boolean activ = true;

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public String getPrenume() { return prenume; }
    public void setPrenume(String prenume) { this.prenume = prenume; }

    public AdresaEmail getEmail() { return email; }
    public void setEmail(AdresaEmail email) { this.email = email; }

    public String getHashParola() { return hashParola; }
    public void setHashParola(String hashParola) { this.hashParola = hashParola; }

    public boolean isActiv() { return activ; }
    public void setActiv(boolean activ) { this.activ = activ; }

    public abstract Rol getRol();

}
