package tms.domain.entity;

import jakarta.persistence.*;
import tms.domain.enums.Rol;

/** Utilizator cu rolul CONTABIL. */
@Entity
@DiscriminatorValue("CONTABIL")
public class Contabil extends Utilizator {

    @Override
    public Rol getRol() { return Rol.CONTABIL; }

}
