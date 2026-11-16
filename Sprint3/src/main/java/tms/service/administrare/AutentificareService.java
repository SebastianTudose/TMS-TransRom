package tms.service.administrare;

import tms.domain.entity.Utilizator;

/** Autentificarea (UC-01, RB-39, RB-40). */
public interface AutentificareService {

    /** Întoarce contul dacă emailul și hash-ul parolei corespund unui cont activ; altfel aruncă BusinessRuleException. */
    Utilizator autentifica(String email, String hashParola);
}
