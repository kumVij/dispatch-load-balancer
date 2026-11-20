package com.dispatch.loadbalancer.strategy;

import com.dispatch.loadbalancer.exception.InvalidRequestException;
import com.dispatch.loadbalancer.util.HaversineCalculator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DispatchStrategyFactoryTest {

    private final HaversineCalculator haversine = new HaversineCalculator();
    private final DispatchStrategyFactory factory = new DispatchStrategyFactory(
            List.of(new PriorityOnlyStrategy(haversine), new GreedyNearestVehicleStrategy(haversine)));

    @Test
    void blankOrNullNameResolvesToTheDefault() {
        assertEquals("greedy", factory.resolve(null).name());
        assertEquals("greedy", factory.resolve("  ").name());
    }

    @Test
    void namesAreCaseInsensitive() {
        assertEquals("priority-only", factory.resolve("PRIORITY-ONLY").name());
    }

    @Test
    void unknownNameListsTheAvailableOnes() {
        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () -> factory.resolve("nope"));
        assertTrue(ex.getMessage().contains("Available"));
    }

    @Test
    void listsAllStrategies() {
        assertEquals(2, factory.available().size());
    }
}
