package tms.domain.enums;

/** Starea foii de parcurs. */
public enum StatusDocument implements StareCuTranzitii<StatusDocument> {
    INTOCMIT, APROBAT, ANULAT;

    @Override
    public boolean poateTrecaIn(StatusDocument tinta) {
        return switch (this) {
            case INTOCMIT -> tinta == APROBAT || tinta == ANULAT;
            case APROBAT -> tinta == INTOCMIT;
            case ANULAT -> false;
        };
    }
}
