package tms.domain.exception;

/** O încălcare a unei reguli de business: codul regulii (ex. RB-21) și mesajul. */
public record BusinessRuleViolation(String cod, String mesaj) {
    @Override public String toString() { return cod + ": " + mesaj; }
}
