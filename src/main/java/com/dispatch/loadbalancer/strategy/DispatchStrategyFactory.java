package com.dispatch.loadbalancer.strategy;

import com.dispatch.loadbalancer.exception.InvalidRequestException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Factory/registry: resolves a strategy by name; every {@link DispatchStrategy} bean registers itself. */
@Component
public class DispatchStrategyFactory {

    public static final String DEFAULT = GreedyNearestVehicleStrategy.NAME;

    private final Map<String, DispatchStrategy> strategies = new LinkedHashMap<>();

    public DispatchStrategyFactory(List<DispatchStrategy> available) {
        available.stream()
                .sorted(java.util.Comparator.comparing(DispatchStrategy::name))
                .forEach(s -> strategies.put(s.name().toLowerCase(Locale.ROOT), s));
    }

    public DispatchStrategy resolve(String name) {
        String key = (name == null || name.isBlank()) ? DEFAULT : name.trim().toLowerCase(Locale.ROOT);
        DispatchStrategy strategy = strategies.get(key);
        if (strategy == null) {
            throw new InvalidRequestException(
                    "Unknown strategy '" + name + "'. Available: " + strategies.keySet());
        }
        return strategy;
    }

    public List<DispatchStrategy> available() {
        return List.copyOf(strategies.values());
    }
}
