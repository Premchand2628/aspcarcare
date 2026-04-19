package com.carwashcommon.routing;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Auto-configuration that replaces the single default DataSource with an
 * {@link EnvironmentRoutingDataSource} backed by two HikariCP pools
 * (prod + qa).
 * <p>
 * Activates <b>only</b> when {@code routing.qa.datasource.url} is set
 * (i.e. on the server, not during local development).
 * <p>
 * Runs <b>before</b> Spring Boot's {@link DataSourceAutoConfiguration} so that
 * the routing DataSource is registered first and the default auto-config backs off.
 */
@AutoConfiguration(before = DataSourceAutoConfiguration.class)
@ConditionalOnClass({DataSource.class, HikariDataSource.class})
@ConditionalOnProperty(name = "routing.qa.datasource.url")
public class RoutingDataSourceAutoConfig {

    @Bean
    public DataSource dataSource(Environment env) {
        // ── Production pool ────────────────────────────────────
        HikariDataSource prodDs = new HikariDataSource();
        prodDs.setJdbcUrl(env.getProperty("spring.datasource.url"));
        prodDs.setUsername(env.getProperty("spring.datasource.username"));
        prodDs.setPassword(env.getProperty("spring.datasource.password"));
        prodDs.setDriverClassName(
                env.getProperty("spring.datasource.driver-class-name",
                        "org.postgresql.Driver"));
        prodDs.setMaximumPoolSize(5);
        prodDs.setMinimumIdle(2);
        prodDs.setPoolName("prod-pool");

        // ── QA pool (smaller — only qa/stg traffic) ───────────
        HikariDataSource qaDs = new HikariDataSource();
        qaDs.setJdbcUrl(env.getProperty("routing.qa.datasource.url"));
        qaDs.setUsername(env.getProperty("routing.qa.datasource.username"));
        qaDs.setPassword(env.getProperty("routing.qa.datasource.password"));
        qaDs.setDriverClassName(
                env.getProperty("spring.datasource.driver-class-name",
                        "org.postgresql.Driver"));
        qaDs.setMaximumPoolSize(3);
        qaDs.setMinimumIdle(1);
        qaDs.setPoolName("qa-pool");

        // ── Routing wrapper ────────────────────────────────────
        EnvironmentRoutingDataSource routingDs = new EnvironmentRoutingDataSource();
        routingDs.setTargetDataSources(Map.of("prod", prodDs, "qa", qaDs));
        routingDs.setDefaultTargetDataSource(prodDs);
        return routingDs;
    }

    @Bean
    public FilterRegistrationBean<EnvironmentFilter> environmentFilterRegistration() {
        FilterRegistrationBean<EnvironmentFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new EnvironmentFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);   // before security / logging
        return reg;
    }
}
