# TMS – Transport Management System (DAM, Master SIA, anul I, UAIC FEAA)

Proiect Maven (Java 17+, Spring 6.2, Spring Data JPA 3.4, Hibernate 6.6, H2, JUnit 5).

    mvn test

## Sprint 3 – Persistence Model Implementation
- `persistence.entity | converter | listener` – `AuditEntry`, `SecventaCod`, convertoare JPA, listeneri (audit, calcule RB-43/44/45, RB-33)
- `persistence.repository` – repository-uri Spring Data JPA (finder-e și `@Query`)
- `persistence.jpa` – bonus: `IRepository` + implementări cu `EntityManager`
- `persistence.config` – `DataSourceConfig` (H2 în memorie), `JpaConfig`
- `service.administrare | raportare` – servicii noi (UC-01, UC-10, UC-13…16)
- Entitățile din `domain` sunt mapate JPA; serviciile și fabricile folosesc repository-uri.

## Structura repository-ului

| Cale | Conținut |
|---|---|
| `src/main/java` | Sprint 2 (domeniu) + Sprint 3 (persistență) |
| `src/test/java` | Teste JUnit 5 (98): validare, calcul, workflow, persistență |

Etichete Git sugerate: `sprint-1`, `sprint-2` (codul din arhiva `tms-domain.zip`), `sprint-3`.

> Notă: testele de persistență nu au fost rulate pe Hibernate/H2 la momentul predării (Maven Central inaccesibil); rulați `mvn test` la prima utilizare.
