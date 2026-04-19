package com.carwashcommon.routing;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Routes to "prod" or "qa" DataSource based on the value stored in
 * {@link EnvironmentContext} (set per-request by {@link EnvironmentFilter}).
 */
public class EnvironmentRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return EnvironmentContext.get();
    }
}
