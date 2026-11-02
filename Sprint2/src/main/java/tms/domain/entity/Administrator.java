package tms.domain.entity;

import tms.domain.enums.Rol;

/** Utilizator cu rolul ADMINISTRATOR. */
public class Administrator extends Utilizator {

    @Override
    public Rol getRol() { return Rol.ADMINISTRATOR; }

}
