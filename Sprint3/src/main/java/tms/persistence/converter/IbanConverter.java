package tms.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tms.domain.vo.Iban;

/** Convertor JPA: obiectul valoare Iban ↔ coloană text (IBAN). */
@Converter
public class IbanConverter implements AttributeConverter<Iban, String> {

    @Override
    public String convertToDatabaseColumn(Iban atribut) {
        return atribut == null ? null : atribut.valoare();
    }

    @Override
    public Iban convertToEntityAttribute(String coloana) {
        return coloana == null ? null : new Iban(coloana);
    }
}
