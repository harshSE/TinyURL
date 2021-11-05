package com.harshse.tinyurl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;

@Configuration
public class RedisConfiguration {

  @Bean
  public ReactiveValueOperations<String, String> cartOperation(
      ReactiveRedisOperations<String, String> operation) {
    return operation.opsForValue();
  }

  @Bean
  public TinyUrlRepository repository(ReactiveValueOperations<String, String> operations) {
    return new TinyUrlRepository(operations);
  }
}