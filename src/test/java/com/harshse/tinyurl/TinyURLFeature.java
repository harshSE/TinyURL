package com.harshse.tinyurl;

import com.sun.net.httpserver.Headers;
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
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import static com.jayway.jsonpath.JsonPath.compile;
import static com.jayway.jsonpath.JsonPath.parse;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TinyURLFeature {

    @LocalServerPort
    private int port;

    private WebClient client;

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

        assertThat(postResponse).map(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.CREATED);
        assertThatJson(postResponse.get().getBody())
                .node("tiny").isString().startsWith("https://www.tinyurl.com/");

        String tinyUrl = parse(postResponse.get().getBody()).read(compile("$.tiny"));

        Optional<ResponseEntity<String>> getResponse = client.get()
                .uri(tinyUrl)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                .blockOptional(Duration.ofMillis(1000));

        assertThat(getResponse).map(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.MULTIPLE_CHOICES);
        assertThat(getResponse).map(ResponseEntity::getHeaders)
                .map(header -> header.get(HttpHeaders.LINK))
                .asList()
                .containsOnly(String.format("%s;rel=\"alternate\"",url));
    }

}
