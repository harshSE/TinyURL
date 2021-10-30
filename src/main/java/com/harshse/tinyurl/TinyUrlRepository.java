package com.harshse.tinyurl;

import reactor.core.publisher.Mono;

public class TinyUrlRepository {

  public Mono<Url> putIfAbsent(SaveRequest request) {
    throw new UnsupportedOperationException("putIfAbsent not supported");
  }
}
