package com.harshse.tinyurl;

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
        Url tinyUrl = tinyUrlGenerator.generate(request.url());
        return tinyUrlRepository.putIfAbsent(new SaveRequest(request.url(), tinyUrl))
            .map(url -> new UrlConversionResponse(request.url(), url));

    }

    public Mono<UrlConversionResponse> deConvert(UrlConversionRequest request) {
        throw new UnsupportedOperationException("deConvert not supported");
    }
}
