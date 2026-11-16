package tms.domain.entity;

import jakarta.persistence.*;
import tms.domain.enums.Rol;

/** Utilizator cu rolul ADMINISTRATOR. */
@Entity
@DiscriminatorValue("ADMINISTRATOR")
public class Administrator extends Utilizator {

    @Override
    public Rol getRol() { return Rol.ADMINISTRATOR; }

}
