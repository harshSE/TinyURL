package com.harshse.tinyurl;

import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

public class TinyUrlRepository {

  private final ReactiveValueOperations<String, String> operations;

  public TinyUrlRepository(ReactiveValueOperations<String, String> operations) {
    this.operations = operations;
  }

  public Mono<Url> putIfAbsent(SaveRequest request) {
    return operations.setIfAbsent(request.key().value(), request.value().value())
        .flatMap(res -> res ? Mono.just(request.value())
            : operations.get(request.key().value()).cast(String.class).map(Url::new));
  }

  public Mono<Url> put(SaveRequest request) {
    return operations.set(request.key().value(), request.value().value())
        .map(res -> request.value());
  }


  public Mono<Url> get(GetRequest getRequest) {
    return operations.get(getRequest.url().value()).map(Url::new);
  }
}
