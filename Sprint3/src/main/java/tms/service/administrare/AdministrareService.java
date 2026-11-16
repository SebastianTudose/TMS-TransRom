package tms.service.administrare;

import tms.domain.entity.Camion;
import tms.domain.entity.Client;
import tms.domain.entity.Utilizator;

/** Administrarea conturilor, clienților și flotei (UC-13, UC-14, UC-15). Toate cer rolul ADMINISTRATOR. */
public interface AdministrareService {
    Utilizator creeazaCont(Utilizator administrator, Utilizator cont);                     // UC-13, RB-01, RB-02
    void dezactiveazaCont(Utilizator administrator, Long contId);                          // UC-13, RB-40
    void reactiveazaCont(Utilizator administrator, Long contId);                           // UC-13
    Client creeazaClient(Utilizator administrator, Client client);                         // UC-14, RB-03..05
    Client modificaClient(Utilizator administrator, Client client);                        // UC-14
    Camion inregistreazaCamion(Utilizator administrator, Camion camion);                   // UC-15, RB-06, RB-07
    void trimiteInService(Utilizator administrator, Long camionId);                        // UC-15, RB-21
    void revineDinService(Utilizator administrator, Long camionId);                        // UC-15
}
