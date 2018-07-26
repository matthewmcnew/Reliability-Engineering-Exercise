package com.mattcnew.reliability;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

class ImageProcessor {

    private Random random = new Random();

    Mono<Message> retrieveImage() {
        return Mono.just(new Message("Sometimes I need to process an image"))
                .delayElement(occasionalWait());
    }


    private Duration occasionalWait() {
        int oneInAHundred = this.random.nextInt(110);
        if (oneInAHundred == 100) {
            //whoops hold up.
            //gotta process this
            return Duration.ofMillis(4000);
        }

        return Duration.ofMillis(this.random.nextInt(20));
    }
}
