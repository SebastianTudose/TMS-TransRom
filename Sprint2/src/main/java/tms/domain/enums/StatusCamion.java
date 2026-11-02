package tms.domain.enums;

/** Starea tehnică a unui camion. */
public enum StatusCamion implements StareCuTranzitii<StatusCamion> {
    DISPONIBIL, IN_CURSA, IN_SERVICE;

    @Override
    public boolean poateTrecaIn(StatusCamion tinta) {
        return switch (this) {
            case DISPONIBIL -> tinta == IN_CURSA || tinta == IN_SERVICE;
            case IN_CURSA -> tinta == DISPONIBIL;
            case IN_SERVICE -> tinta == DISPONIBIL;
        };
    }
}
