package tms.domain.support;

/** Serviciu de suport: generează coduri unice și lizibile (RB-20), ex. RT-2026-0001. */
public interface SecventaCoduriService {
    String urmatorulCod(String prefix);
}
