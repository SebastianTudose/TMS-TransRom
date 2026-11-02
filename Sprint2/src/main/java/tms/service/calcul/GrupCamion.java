package tms.service.calcul;

import java.math.BigDecimal;
import java.util.List;

public record GrupCamion(String nrInmatriculare, List<RandCentralizator> randuri,
                         BigDecimal distantaTotala, BigDecimal costTotal) {
}
