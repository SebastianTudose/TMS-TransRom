package tms.domain.enums;

/** Starea unei facturi. */
public enum StatusFactura implements StareCuTranzitii<StatusFactura> {
    EMISA, INCASATA, STORNATA;

    @Override
    public boolean poateTrecaIn(StatusFactura tinta) {
        return switch (this) {
            case EMISA -> tinta == INCASATA || tinta == STORNATA;
            case INCASATA -> false;
            case STORNATA -> false;
        };
    }
}
