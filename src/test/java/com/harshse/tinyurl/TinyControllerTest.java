package com.harshse.tinyurl;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(TinyURLController.class)
public class TinyControllerTest {


  private WebTestClient client  = WebTestClient.bindToController(TinyURLController.class).build();

  @MockBean
  private TinyUrlService service;



  @Test
  public void generateTinyURL() {

    String url = "https://test.com";

    client.post()
        .bodyValue(url)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CREATED)
        .expectBody(TinyUrlResponse.class).isEqualTo(
            new TinyUrlResponse(url, "https://tinyurl.com/abc")
        );
  }

}
