package com.harshse.tinyurl;

import static com.harshse.tinyurl.TinyUrlServiceTest.ACTUAL_URL2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import io.lettuce.core.FlushMode;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = NONE)
public class TinyUrlRepoTest {

  public static final Url ACTUAL_URL = new Url("https://www.test.com");
  public static final Url TINY_URL = new Url("https://localhost:8080/abc");
  public static final Url TINY_URL2 = new Url("https://localhost:8080/xyz");

  @Container
  static public GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:6.2.6-alpine"))
      .withExposedPorts(6379);

  @DynamicPropertySource
  public static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.redis.port", redis::getFirstMappedPort);
  }


  @Autowired
  TinyUrlRepository repository;

  private static StatefulRedisConnection<String, String> connection;
  private static RedisCommands<String, String> syncCommands;

  @BeforeAll
  static void setUp() {

    RedisClient redisClient = RedisClient
        .create("redis://localhost:%s/".formatted(redis.getFirstMappedPort()));
    connection = redisClient.connect();
    syncCommands = connection.sync();
  }

  @Test
  public void storeTheValueAgainstKey() {
    repository.put(new SaveRequest(ACTUAL_URL, TINY_URL)).block();
    assertThat(syncCommands.get(ACTUAL_URL.value())).isEqualTo(TINY_URL.value());
  }

  @Test
  public void storeTheValueAgainstKeyWhenKeyIsNotPresent() {
    syncCommands.set(ACTUAL_URL.value(), TINY_URL.value());
    repository.putIfAbsent(new SaveRequest(ACTUAL_URL, TINY_URL2)).block();
    repository.putIfAbsent(new SaveRequest(ACTUAL_URL2, TINY_URL2)).block();
    assertThat(syncCommands.get(ACTUAL_URL.value())).isEqualTo(TINY_URL.value());
    assertThat(syncCommands.get(ACTUAL_URL2.value())).isEqualTo(TINY_URL2.value());
  }

  @Test
  public void returnPreviousStoredKeyWhenKeyAlreadyPresent() {
    syncCommands.set(ACTUAL_URL.value(), TINY_URL.value());
    Url tinyUrl = repository.putIfAbsent(new SaveRequest(ACTUAL_URL, TINY_URL2)).block();
    assertThat(tinyUrl).isEqualTo(TINY_URL);
  }

  @Test
  public void retrieveStoredValueAgainstKey() {
    syncCommands.set(ACTUAL_URL.value(), TINY_URL.value());
    Url url = repository.get(new GetRequest(ACTUAL_URL)).block();
    assertThat(url).isEqualTo(TINY_URL);
  }

  @AfterEach
  void tearDown() {
    syncCommands.flushdb(FlushMode.SYNC);
  }

  @AfterAll
  static void afterAll() {
    connection.close();
  }
}
