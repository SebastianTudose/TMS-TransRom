package tms.service.calcul;

import java.math.BigDecimal;
import java.util.List;
import tms.domain.enums.Moneda;

/** Vedere calculată „Centralizator consum și costuri auto" (RB-48). */
public record CentralizatorConsum(List<GrupCamion> grupuri, BigDecimal distantaTotala,
                                  BigDecimal costTotal, Moneda moneda) {
}
