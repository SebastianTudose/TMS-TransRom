package tms.domain.exception;

import java.util.List;

/** Excepție aruncată când o operație încalcă una sau mai multe reguli de business. */
public class BusinessRuleException extends RuntimeException {
    private final List<BusinessRuleViolation> violations;

    public BusinessRuleException(List<BusinessRuleViolation> violations) {
        super(violations.stream().map(BusinessRuleViolation::toString).reduce((a, b) -> a + "; " + b).orElse("Regulă încălcată"));
        this.violations = List.copyOf(violations);
    }

    public BusinessRuleException(String cod, String mesaj) {
        this(List.of(new BusinessRuleViolation(cod, mesaj)));
    }

    public List<BusinessRuleViolation> getViolations() { return violations; }

    public boolean hasCode(String cod) {
        return violations.stream().anyMatch(v -> v.cod().equals(cod));
    }
}
