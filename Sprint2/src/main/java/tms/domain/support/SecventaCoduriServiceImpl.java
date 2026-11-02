package tms.domain.support;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class SecventaCoduriServiceImpl implements SecventaCoduriService {

    private final Clock clock;
    private final Map<String, AtomicLong> secvente = new ConcurrentHashMap<>();

    public SecventaCoduriServiceImpl(Clock clock) { this.clock = clock; }

    @Override
    public String urmatorulCod(String prefix) {
        int an = LocalDate.now(clock).getYear();
        long nr = secvente.computeIfAbsent(prefix + "-" + an, k -> new AtomicLong()).incrementAndGet();
        return String.format("%s-%d-%04d", prefix, an, nr);
    }
}
