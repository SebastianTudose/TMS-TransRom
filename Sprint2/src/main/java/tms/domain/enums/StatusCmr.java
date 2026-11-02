package tms.domain.enums;

/** Starea documentului CMR. */
public enum StatusCmr implements StareCuTranzitii<StatusCmr> {
    EMIS, ANULAT;

    @Override
    public boolean poateTrecaIn(StatusCmr tinta) {
        return switch (this) {
            case EMIS -> tinta == ANULAT;
            case ANULAT -> false;
        };
    }
}
