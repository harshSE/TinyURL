package com.harshse.tinyurl;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TinyURLFeature {

  @LocalServerPort
  private int port;

  private WebClient client;

  @BeforeEach
  public void setUp() {
    client = WebClient.create("http://localhost:%s".formatted(port));
  }


  @Test
  @DisplayName("I am able to generate tiny url for given url")
  public void generateTinyUrl() {

    String url = "https://www.infoq.com/presentations/microservices-best-practices/?"
        + "utm_source=notification_email&utm_campaign=notifications&utm_medium=link"
        + "&utm_content=content_in_followed_topic&utm_term=daily";

    Optional<ResponseEntity<String>> postResponse = client.post()
        .bodyValue(url)
        .accept(MediaType.APPLICATION_JSON)
        .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
        .blockOptional(Duration.ofMillis(1000));

    assertThat(postResponse).map(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.CREATED);
    assertThatJson(postResponse.get().getBody())
        .node("message").isString().startsWith("https://www.tinyurl.com/");

    Optional<ResponseEntity<String>> getResponse = client.get()
        .accept(MediaType.APPLICATION_JSON)
        .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
        .blockOptional(Duration.ofMillis(1000));

    assertThat(getResponse).map(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.OK);
    assertThatJson(postResponse.get().getBody())
        .node("message").isString().isEqualTo(url);


  }

}
