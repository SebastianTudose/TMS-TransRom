package tms.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tms.domain.enums.Rol;
import tms.domain.vo.AdresaEmail;
import tms.persistence.converter.AdresaEmailConverter;

/** Entitate abstractă: date comune de identificare și autentificare. Fiecare cont are un singur rol. */
@Entity
@Table(name = "utilizator", uniqueConstraints = @UniqueConstraint(name = "uk_utilizator_email", columnNames = "email"))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "rol", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Utilizator extends AbstractEntity {

    @NotBlank(message = "RB-02: Numele este obligatoriu")
    private String nume;

    @NotBlank(message = "RB-02: Prenumele este obligatoriu")
    private String prenume;

    @NotNull(message = "RB-02: Adresa de email este obligatorie")
    @Valid
    @Convert(converter = AdresaEmailConverter.class)
    @Column(name = "email", nullable = false, length = 254)
    private AdresaEmail email;

    @NotBlank(message = "RB-02: Datele de autentificare sunt obligatorii")
    @Column(name = "hash_parola", length = 100)
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
