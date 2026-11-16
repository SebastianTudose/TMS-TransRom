package tms.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tms.domain.vo.NrInmatriculare;

/** Convertor JPA: obiectul valoare NrInmatriculare ↔ coloană text (Număr de înmatriculare). */
@Converter
public class NrInmatriculareConverter implements AttributeConverter<NrInmatriculare, String> {

    @Override
    public String convertToDatabaseColumn(NrInmatriculare atribut) {
        return atribut == null ? null : atribut.valoare();
    }

    @Override
    public NrInmatriculare convertToEntityAttribute(String coloana) {
        return coloana == null ? null : new NrInmatriculare(coloana);
    }
}
