package com.carwashcommon.routing;

/**
 * Thread-local holder for the current environment key (prod / qa).
 * Set by {@link EnvironmentFilter}, read by {@link EnvironmentRoutingDataSource}.
 */
public final class EnvironmentContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private EnvironmentContext() {}

    public static void set(String env) {
        CURRENT.set(env);
    }

    /** Returns "prod" when nothing has been set (safe default). */
    public static String get() {
        String env = CURRENT.get();
        return env != null ? env : "prod";
    }

    public static void clear() {
        CURRENT.remove();
    }
}
