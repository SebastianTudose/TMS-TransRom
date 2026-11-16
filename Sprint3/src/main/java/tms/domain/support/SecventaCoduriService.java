package tms.domain.support;

/** Serviciu de suport: generează coduri unice și lizibile (RB-20), ex. RT-2026-0001; contoarele sunt persistente. */
public interface SecventaCoduriService {
    String urmatorulCod(String prefix);
}
