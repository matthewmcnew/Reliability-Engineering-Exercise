package com.mattcnew.reliability;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebFluxTest
public class ControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private ImageProcessor imageProcessor;

    @MockBean
    private CachedService cachedService;

    @Test
    public void occasionalImageProcessing_getsThoseImages() {
        when(imageProcessor.retrieveImage()).thenReturn(Mono.just(new Message("Image")));

        webClient.get().uri("/occasional-image-processing")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("{\"field\":\"Image\"}");

        verify(imageProcessor).retrieveImage();
    }


    @Test
    public void noLookup_isHappy()  {
        webClient.get().uri("/no-lookup")
                .exchange()
                .expectStatus().isOk();
    }


    @Test
    public void occasionalCache_hitsThatCache() {
        when(cachedService.cached(any())).thenReturn("Cached String");

        webClient.get().uri("/cache-lookup")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("{\"field\": \"Cached String\"}");

        verify(cachedService).cached(anyString());

    }

}