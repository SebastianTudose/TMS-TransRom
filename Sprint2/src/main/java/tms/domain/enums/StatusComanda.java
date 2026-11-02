package tms.domain.enums;

/** Ciclul de viață al unei comenzi de transport. */
public enum StatusComanda implements StareCuTranzitii<StatusComanda> {
    NOUA, PLANIFICATA, IN_EXECUTIE, LIVRATA, FACTURATA, ANULATA;

    @Override
    public boolean poateTrecaIn(StatusComanda tinta) {
        return switch (this) {
            case NOUA -> tinta == PLANIFICATA || tinta == ANULATA;
            case PLANIFICATA -> tinta == NOUA || tinta == IN_EXECUTIE;
            case IN_EXECUTIE -> tinta == LIVRATA || tinta == NOUA;
            case LIVRATA -> tinta == FACTURATA || tinta == IN_EXECUTIE;
            case FACTURATA -> tinta == LIVRATA;
            case ANULATA -> false;
        };
    }
}
