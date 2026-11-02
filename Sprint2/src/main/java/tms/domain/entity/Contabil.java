package tms.domain.entity;

import tms.domain.enums.Rol;

/** Utilizator cu rolul CONTABIL. */
public class Contabil extends Utilizator {

    @Override
    public Rol getRol() { return Rol.CONTABIL; }

}
