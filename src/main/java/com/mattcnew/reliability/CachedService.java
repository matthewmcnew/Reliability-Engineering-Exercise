package com.mattcnew.reliability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CachedService {

    private final Logger logger = LoggerFactory.getLogger(CachedService.class);

    @Cacheable(value = "myCache", sync = true)
    public String cached(String whatDoWeHaveHere) {
        logger.info("Cache Miss");
        try {
            //  In a real Webflux application:
            //  blocking network calls, database lookups, or even computationally expensive operations could achieve the same result
            Thread.sleep(Duration.ofSeconds(20L).toMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("fetching value complete");

        return "Value";
    }
}
