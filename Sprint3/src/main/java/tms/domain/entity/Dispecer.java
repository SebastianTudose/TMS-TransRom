package tms.domain.entity;

import jakarta.persistence.*;
import tms.domain.enums.Rol;

/** Utilizator cu rolul DISPECER. */
@Entity
@DiscriminatorValue("DISPECER")
public class Dispecer extends Utilizator {

    @Override
    public Rol getRol() { return Rol.DISPECER; }

}
