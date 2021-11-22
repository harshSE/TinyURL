package com.harshse.tinyurl;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;

@WebFluxTest(TinyURLController.class)
public class TinyControllerTest {


  private static final Url ACTUAL_URL = new Url("https://test.com");
  private static final Url TINY_URL_PART = new Url("abc");
  private static final Url TINY_URL = new Url("https://tinyurl.com/abc");
  private WebTestClient client;

  @MockBean
  private TinyUrlService service;

  @Autowired
  TinyURLController controller;

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("tinyurl.host", () -> "https://tinyurl.com");
  }

  @BeforeEach
  public void setUp() {

    client  = WebTestClient.bindToController(controller).build();
  }

  @Test
  public void generateTinyURL() {

    UrlConversionRequest request = new UrlConversionRequest(ACTUAL_URL);

    UrlConversionResponse response = new UrlConversionResponse(ACTUAL_URL, TINY_URL_PART);

    when(service.convert(request)).thenReturn(Mono.just(response));

    client.post()
        .bodyValue(ACTUAL_URL.value())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CREATED)
        .expectBody(TinyUrlResponse.class).isEqualTo(
            new TinyUrlResponse(ACTUAL_URL.value(), TINY_URL.value())
        );
  }

  @Test
  public void returnActualUrlForTinyUrl() {

    Url tinyUrlParam = new Url("abc");
    UrlConversionRequest request = new UrlConversionRequest(tinyUrlParam);

    UrlConversionResponse response = new UrlConversionResponse(ACTUAL_URL, tinyUrlParam);

    when(service.deConvert(request)).thenReturn(Mono.just(response));

    client.get().uri("/abc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FOUND)
            .expectHeader()
            .valueEquals(LOCATION, "https://test.com");
  }

}
