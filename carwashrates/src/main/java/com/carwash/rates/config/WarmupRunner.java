package com.carwash.rates.config;

import com.carwash.rates.repository.CarwashServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Pre-warms the JPA / Hibernate / Hikari layers and seeds the @Cacheable
 * "carwashServices" entry on application startup. This eliminates the
 * cold-start latency that the first user would otherwise experience on
 * GET /services.
 */
@Component
public class WarmupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(WarmupRunner.class);

    private final CarwashServiceRepository carwashServiceRepository;

    public WarmupRunner(CarwashServiceRepository carwashServiceRepository) {
        this.carwashServiceRepository = carwashServiceRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        long start = System.currentTimeMillis();
        try {
            int count = carwashServiceRepository.findByActiveTrueOrderBySortOrderAsc().size();
            log.info("Warmup: prefetched {} carwash services in {} ms",
                    count, System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.warn("Warmup: failed to prefetch services ({}). Will lazy-load on first request.",
                    ex.getMessage());
        }
    }
}
