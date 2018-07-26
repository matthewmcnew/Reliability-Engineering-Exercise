package com.mattcnew.reliability;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class Controller {

    private CachedService cachedService;
    private ImageProcessor imageProcessor;


    @Autowired
    public Controller(CachedService cachedService, ImageProcessor imageProcessor) {
        this.cachedService = cachedService;
        this.imageProcessor = imageProcessor;
    }

    @GetMapping("/cache-lookup")
    public Mono<Message> cacheLookup() {

        return Mono.just(new Message(cachedService.cached("keyForCache")))
                .delayElement(Duration.ofMillis(10L));
    }

    @GetMapping("/no-lookup")
    public Mono<Message> noLookup() {
        return Mono.just(
                new Message("No Lookup For Me!"))
                .delayElement(Duration.ofMillis(10L));
    }

    @GetMapping("/occasional-image-processing")
    public Mono<Message> occasionalImageProcessor() {
        return imageProcessor.retrieveImage();
    }
}
