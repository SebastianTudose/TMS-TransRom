package tms.domain.vo;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/** Obiect valoare: interval de timp [inceput, sfarsit). */
public record IntervalTimp(
        @NotNull(message = "RB-09: Începutul intervalului este obligatoriu") LocalDateTime inceput,
        @NotNull(message = "RB-09: Sfârșitul intervalului este obligatoriu") LocalDateTime sfarsit) {

    @AssertTrue(message = "RB-09: Sfârșitul trebuie să fie ulterior începutului")
    public boolean isSfarsitDupaInceput() {
        return inceput == null || sfarsit == null || sfarsit.isAfter(inceput);
    }

    public boolean suprapune(IntervalTimp alt) {
        return inceput.isBefore(alt.sfarsit) && alt.inceput.isBefore(sfarsit);
    }

    public boolean contine(LocalDateTime moment) {
        return !moment.isBefore(inceput) && moment.isBefore(sfarsit);
    }
}
