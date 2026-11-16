package tms.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tms.domain.vo.AdresaEmail;

/** Convertor JPA: obiectul valoare AdresaEmail ↔ coloană text (Adresa de email). */
@Converter
public class AdresaEmailConverter implements AttributeConverter<AdresaEmail, String> {

    @Override
    public String convertToDatabaseColumn(AdresaEmail atribut) {
        return atribut == null ? null : atribut.valoare();
    }

    @Override
    public AdresaEmail convertToEntityAttribute(String coloana) {
        return coloana == null ? null : new AdresaEmail(coloana);
    }
}
