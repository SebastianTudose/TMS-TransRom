package tms.domain.entity;

import tms.domain.enums.Rol;

/** Utilizator cu rolul DISPECER. */
public class Dispecer extends Utilizator {

    @Override
    public Rol getRol() { return Rol.DISPECER; }

}
