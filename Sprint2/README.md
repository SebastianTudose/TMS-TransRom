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

<img width="2623" height="2158" alt="Fig5_Diagrama_Clase_Domeniu" src="https://github.com/user-attachments/assets/90b0b45a-4b8d-4f5a-91cc-705ccee3ce24" />

<img width="2352" height="1545" alt="Fig6_Model_Relational_ER" src="https://github.com/user-attachments/assets/88180985-c803-4b4b-8f7b-c6c6b51b4863" />

