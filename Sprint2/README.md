# TMS – Sprint 2: Domain Implementation

Proiect Maven (Java 17+, Spring 6, Jakarta Bean Validation, JUnit 5).

    mvn test

Pachete:
- `domain.entity`, `domain.vo`, `domain.enums` – modelul domeniului (entități, agregate, obiecte valoare, enumerări)
- `domain.support` – fabrici de agregate și secvențe de coduri
- `service.validation | calcul | audit | workflow` – serviciile de business
- `config.TmsConfig` – clasa de configurare Spring
- `src/test` – teste JUnit pentru validare, calcul și workflow

---
Rulare teste: `mvn test` (Java 17+, Maven 3.9+).
