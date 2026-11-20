package com.dispatch.loadbalancer.controller;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class TestPayloads {

    private TestPayloads() {
    }

    static String order(String id, double lat, double lon, double weight, String priority) {
        return String.format(Locale.ROOT,
                "{\"orderId\":\"%s\",\"latitude\":%s,\"longitude\":%s,\"address\":\"addr %s\","
                        + "\"packageWeight\":%s,\"priority\":\"%s\"}", id, lat, lon, id, weight, priority);
    }

    static String orders(String... orderJson) {
        return "{\"orders\":[" + Stream.of(orderJson).collect(Collectors.joining(",")) + "]}";
    }

    static String vehicle(String id, double capacity, double lat, double lon) {
        return String.format(Locale.ROOT,
                "{\"vehicleId\":\"%s\",\"capacity\":%s,\"currentLatitude\":%s,\"currentLongitude\":%s,"
                        + "\"currentAddress\":\"depot %s\"}", id, capacity, lat, lon, id);
    }

    static String vehicles(String... vehicleJson) {
        return "{\"vehicles\":[" + Stream.of(vehicleJson).collect(Collectors.joining(",")) + "]}";
    }
}
