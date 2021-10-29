package com.harshse.tinyurl;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LINK;

@WebFluxTest(TinyURLController.class)
public class TinyControllerTest {


  private WebTestClient client;

  @MockBean
  private TinyUrlService service;

  @Autowired
  TinyURLController controller;

  @BeforeEach
  public void setUp() {

    client  = WebTestClient.bindToController(controller).build();
  }

  @Test
  public void generateTinyURL() {

    String url = "https://test.com";
    String tinyUrl = "https://tinyurl.com/abc";

    UrlConversionRequest request = new UrlConversionRequest(url);

    UrlConversionResponse response = new UrlConversionResponse(url, tinyUrl);

    when(service.convert(request)).thenReturn(Mono.just(response));

    client.post()
        .bodyValue(url)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CREATED)
        .expectBody(TinyUrlResponse.class).isEqualTo(
            new TinyUrlResponse(url, tinyUrl)
        );
  }

  @Test
  public void returnActualUrlForTinyUrl() {

    String url = "https://test.com";
    String tinyUrl = "https://tinyurl.com/abc";

    UrlConversionRequest request = new UrlConversionRequest("abc");

    UrlConversionResponse response = new UrlConversionResponse(url, tinyUrl);

    when(service.deConvert(request)).thenReturn(Mono.just(response));

    client.get().uri("/abc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.MULTIPLE_CHOICES)
            .expectHeader()
            .valueEquals(LINK, String.format("<%s>;rel=\"alternate\"", url));
  }

}
