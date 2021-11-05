package com.harshse.tinyurl;

import java.util.stream.Stream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TinyUrlService {

  private final TinyUrlGenerator tinyUrlGenerator;
  private final TinyUrlRepository tinyUrlRepository;

  public TinyUrlService(
      TinyUrlGenerator tinyUrlGenerator,
      TinyUrlRepository tinyUrlRepository) {
    this.tinyUrlGenerator = tinyUrlGenerator;
    this.tinyUrlRepository = tinyUrlRepository;
  }

  public Mono<UrlConversionResponse> convert(UrlConversionRequest request) {

    return Flux.fromStream(Stream.generate(() -> tinyUrlGenerator.generate(request.url())))
        .filterWhen(url -> tinyUrlRepository.get(new GetRequest(url))
            .defaultIfEmpty(url)
            .map(actualUlr -> url == actualUlr))
        .next()
        .flatMap(tinyUrl -> tinyUrlRepository.putIfAbsent(new SaveRequest(request.url(), tinyUrl))
            .zipWhen(url -> {
                  if (url.equals(tinyUrl)) {
                    //FIXME what happen when this operation fail.
                    return tinyUrlRepository.put(new SaveRequest(tinyUrl, request.url()));
                  } else {
                    return Mono.just(request.url());
                  }
                },
                (url, url2) -> url)
            .map(url -> new UrlConversionResponse(request.url(), url))
        );

    }

    public Mono<UrlConversionResponse> deConvert (UrlConversionRequest request){
      return tinyUrlRepository.get(new GetRequest(request.url()))
          .map(url -> new UrlConversionResponse(url, request.url()));
    }


  }
