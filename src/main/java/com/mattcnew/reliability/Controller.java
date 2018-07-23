package com.mattcnew.reliability;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class Controller {

    private CachedService cachedService;

    @Autowired
    public Controller(CachedService cachedService) {
        this.cachedService = cachedService;
    }

    @GetMapping("/cache-lookup")
    public Mono<Message> cacheLookup() {
        return Mono.just(
                new Message(cachedService.cached("keyForCache")))
                .delayElement(java.time.Duration.ofMillis(100L));
    }

    @GetMapping("/no-lookup")
    public Mono<Message> noLookup() {
        return Mono.just(
                new Message("No Lookup For Me!"))
                .delayElement(java.time.Duration.ofMillis(100L));
    }
}
