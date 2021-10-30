package com.harshse.tinyurl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class TinyUrlServiceTest {

    public static final Url ACTUAL_URL = new Url("https://www.test.com");
    public static final Url TINY_URL = new Url("abc");
    public static final Url TINY_URL2 = new Url("xyz");

    @Mock
    private TinyUrlGenerator tinyUrlGenerator;

    @Mock
    private TinyUrlRepository repository;

    @InjectMocks
    private TinyUrlService tinyUrlService;

    @Test
    public void convertGivenUrlToTinyUrl() {

        UrlConversionRequest request = new UrlConversionRequest(ACTUAL_URL);

        when(tinyUrlGenerator.generate(ACTUAL_URL)).thenReturn(TINY_URL);

        when(repository.putIfAbsent(new SaveRequest(ACTUAL_URL, TINY_URL)))
            .thenReturn(Mono.just(TINY_URL));

        UrlConversionResponse response = tinyUrlService.convert(request).block();

        assertThat(response).isEqualTo(new UrlConversionResponse(ACTUAL_URL, TINY_URL));
    }

    @Test
    public void returnSameUrlWhenTinyUrlIsAlreadyGenerated() {

        UrlConversionRequest request = new UrlConversionRequest(ACTUAL_URL);

        when(tinyUrlGenerator.generate(ACTUAL_URL))
                .thenReturn(TINY_URL);

        when(repository.putIfAbsent(new SaveRequest(ACTUAL_URL, TINY_URL)))
            .thenReturn(Mono.just(TINY_URL2));

        UrlConversionResponse response = tinyUrlService.convert(request).block();

        assertThat(response).isEqualTo(new UrlConversionResponse(ACTUAL_URL, TINY_URL2));
    }

}