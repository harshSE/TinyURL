package com.harshse.tinyurl;

import static com.jayway.jsonpath.JsonPath.compile;
import static com.jayway.jsonpath.JsonPath.parse;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TinyURLFeature {

  private static final String PREFIX = "https://www.tinyurl.com/";
  @LocalServerPort
  private int port;

  private WebClient client;

  @Container
  static public GenericContainer<?> redis = new GenericContainer<>(
      DockerImageName.parse("redis:6.2.6-alpine"))
      .withExposedPorts(6379);

  @DynamicPropertySource
  public static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("REDIS_PORT", redis::getFirstMappedPort);
  }

  @BeforeEach
  public void setUp() {
    client = WebClient.create(String.format("http://localhost:%s", port));
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

    assertThat(postResponse).map(ResponseEntity::getStatusCode).hasValue(HttpStatus.CREATED);
    assertThatJson(postResponse.get().getBody())
        .node("tiny").isString().startsWith(PREFIX);

    String tinyUrl = parse(postResponse.get().getBody()).read(compile("$.tiny"));
    tinyUrl = tinyUrl.substring(tinyUrl.indexOf(PREFIX) + PREFIX.length());

    Optional<ResponseEntity<String>> getResponse = client.get()
        .uri(tinyUrl)
        .accept(MediaType.APPLICATION_JSON)
        .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
        .blockOptional(Duration.ofMillis(1000));

    assertThat(getResponse).map(ResponseEntity::getStatusCode)
        .hasValue(HttpStatus.MULTIPLE_CHOICES);
    assertThat(getResponse).map(ResponseEntity::getHeaders)
        .map(header -> header.get(HttpHeaders.LINK))
        .hasValue(Arrays.asList(String.format("<%s>;rel=\"alternate\"", url)));
  }

}
