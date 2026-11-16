package tms.domain.enums;

/** Contract pentru enumerările care definesc un ciclu de viață (automat de stări). */
public interface StareCuTranzitii<E extends Enum<E>> {
    boolean poateTrecaIn(E tinta);
}
