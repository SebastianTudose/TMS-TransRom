package tms.persistence.entity;

import jakarta.persistence.*;

/** Contor persistent pentru codurile unice (RB-20): câte un rând pentru fiecare pereche prefix + an, ex. „RT-2026”. */
@Entity
@Table(name = "secventa_cod", uniqueConstraints = @UniqueConstraint(name = "uk_secventa_cheie", columnNames = "cheie"))
public class SecventaCod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cheie", nullable = false, length = 40)
    private String cheie;

    @Column(name = "ultima_valoare", nullable = false)
    private long ultimaValoare;

    @Version
    private Integer version;

    protected SecventaCod() { }

    public SecventaCod(String cheie) { this.cheie = cheie; }

    public String getCheie() { return cheie; }

    public long getUltimaValoare() { return ultimaValoare; }

    /** Incrementează contorul și întoarce noua valoare. */
    public long urmatoareaValoare() { return ++ultimaValoare; }
}
