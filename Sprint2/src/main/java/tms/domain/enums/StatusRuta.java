package tms.domain.enums;

/** Ciclul de viață al unei curse (rute). */
public enum StatusRuta implements StareCuTranzitii<StatusRuta> {
    PLANIFICATA, IN_TRANZIT, FINALIZATA, ANULATA;

    @Override
    public boolean poateTrecaIn(StatusRuta tinta) {
        return switch (this) {
            case PLANIFICATA -> tinta == IN_TRANZIT || tinta == ANULATA;
            case IN_TRANZIT -> tinta == FINALIZATA || tinta == ANULATA;
            case FINALIZATA -> tinta == IN_TRANZIT;
            case ANULATA -> false;
        };
    }

    public boolean esteActiva() { return this == PLANIFICATA || this == IN_TRANZIT; }
}
