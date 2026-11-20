package com.dispatch.loadbalancer.model;

/**
 * Order priority. Declaration order matters: the dispatch strategy sorts by
 * {@code ordinal()}, so HIGH is processed first.
 */
public enum Priority {
    HIGH,
    MEDIUM,
    LOW
}
