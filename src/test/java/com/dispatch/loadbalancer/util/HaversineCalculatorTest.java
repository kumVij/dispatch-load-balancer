package com.dispatch.loadbalancer.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HaversineCalculatorTest {

    private final HaversineCalculator calculator = new HaversineCalculator();

    @Test
    void samePointIsZeroDistance() {
        assertEquals(0.0, calculator.distanceKm(28.6139, 77.2090, 28.6139, 77.2090), 1e-9);
    }

    @Test
    void bangaloreToChennaiIsAbout290Km() {
        double d = calculator.distanceKm(12.9716, 77.5946, 13.0827, 80.2707);
        assertEquals(290.0, d, 5.0);
    }

    @Test
    void distanceIsSymmetric() {
        double ab = calculator.distanceKm(28.6139, 77.2090, 28.5355, 77.3910);
        double ba = calculator.distanceKm(28.5355, 77.3910, 28.6139, 77.2090);
        assertEquals(ab, ba, 1e-9);
    }

    @Test
    void oneDegreeOfLatitudeIsAbout111Km() {
        assertEquals(111.2, calculator.distanceKm(0, 0, 1, 0), 0.5);
    }
}
