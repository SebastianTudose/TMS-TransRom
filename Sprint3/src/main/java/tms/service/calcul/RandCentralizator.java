package tms.service.calcul;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import tms.domain.vo.SumaMonetara;

public record RandCentralizator(String codRuta, BigDecimal distanta, BigDecimal consumLitri,
                                SumaMonetara pretPerLitru, SumaMonetara cost, LocalDateTime dataCalcul) {
}
