package tms.domain.vo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import tms.domain.enums.Moneda;

/** Obiect valoare: sumă și monedă. Nu are identitate; se compară prin conținut. */
public record SumaMonetara(
        @NotNull(message = "RB-10: Valoarea este obligatorie")
        @DecimalMin(value = "0.00", message = "RB-10: Valoarea nu poate fi negativă")
        BigDecimal valoare,
        @NotNull(message = "RB-10: Moneda este obligatorie")
        Moneda moneda) {

    public static SumaMonetara of(String valoare, Moneda moneda) { return new SumaMonetara(new BigDecimal(valoare), moneda); }
    public static SumaMonetara zero(Moneda moneda) { return new SumaMonetara(BigDecimal.ZERO.setScale(2), moneda); }

    public SumaMonetara plus(SumaMonetara alta) {
        if (alta.moneda != moneda) throw new IllegalArgumentException("Monede diferite: " + moneda + " / " + alta.moneda);
        return new SumaMonetara(valoare.add(alta.valoare).setScale(2, RoundingMode.HALF_UP), moneda);
    }

    public SumaMonetara inmultit(BigDecimal factor) {
        return new SumaMonetara(valoare.multiply(factor).setScale(2, RoundingMode.HALF_UP), moneda);
    }
}
