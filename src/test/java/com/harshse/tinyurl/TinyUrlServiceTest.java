package com.harshse.tinyurl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class TinyUrlServiceTest {

  public static final Url ACTUAL_URL = new Url("https://www.test.com");
  public static final Url ACTUAL_URL2 = new Url("https://www.test2.com");
  public static final Url TINY_URL = new Url("abc");
  public static final Url TINY_URL2 = new Url("xyz");

  @Mock
  private TinyUrlGenerator tinyUrlGenerator;

  @Mock
  private TinyUrlRepository repository;

  @InjectMocks
  private TinyUrlService tinyUrlService;

  @Nested
  public class TinyUrlToActualUrlConversion {

    private UrlConversionRequest request;

    @BeforeEach
    public void setUp() {
      request = new UrlConversionRequest(TINY_URL);
    }

    @Test
    public void returnActualUrlFromTinyUrl() {

      when(repository.get(new GetRequest(TINY_URL))).thenReturn(Mono.just(ACTUAL_URL));

      UrlConversionResponse response = tinyUrlService.deConvert(request).block();

      assertThat(response).isEqualTo(new UrlConversionResponse(ACTUAL_URL, TINY_URL));

    }

  }


  @Nested
  public class ActualUrlToTinyUrlConversion {

    private UrlConversionRequest request;

    @BeforeEach
    public void setUp() {
      request = new UrlConversionRequest(ACTUAL_URL);

      lenient().when(repository.get(any(GetRequest.class)))
          .thenReturn(Mono.empty());
    }


    @Test
    public void convertGivenUrlToTinyUrl() {

      when(tinyUrlGenerator.generate(ACTUAL_URL)).thenReturn(TINY_URL);

      when(repository.putIfAbsent(any(SaveRequest.class)))
          .thenReturn(Mono.just(TINY_URL));

      when(repository.put(any(SaveRequest.class)))
          .thenReturn(Mono.just(ACTUAL_URL));

      UrlConversionResponse response = tinyUrlService.convert(request).block();

      assertThat(response).isEqualTo(new UrlConversionResponse(ACTUAL_URL, TINY_URL));
    }

    @Test
    public void storeTinyUrlToActualUrl() {

      when(tinyUrlGenerator.generate(ACTUAL_URL)).thenReturn(TINY_URL);

      when(repository.putIfAbsent(new SaveRequest(ACTUAL_URL, TINY_URL)))
          .thenReturn(Mono.just(TINY_URL));

      when(repository.put(new SaveRequest(TINY_URL, ACTUAL_URL)))
          .thenReturn(Mono.just(ACTUAL_URL));

      tinyUrlService.convert(request).block();

      verify(repository).put(new SaveRequest(TINY_URL, ACTUAL_URL));
    }

    @Test
    public void regenerateTinyUrlUnTillUniqueUrlNotGenerated() {

      lenient().when(tinyUrlGenerator.generate(ACTUAL_URL))
          .thenReturn(TINY_URL)
          .thenReturn(TINY_URL2)
          .thenThrow(RuntimeException.class);

      when(repository.get(new GetRequest(TINY_URL)))
          .thenReturn(Mono.just(ACTUAL_URL2));

      when(repository.get(new GetRequest(TINY_URL2)))
          .thenReturn(Mono.empty());

      when(repository.putIfAbsent(any(SaveRequest.class)))
          .thenReturn(Mono.just(TINY_URL2));

      when(repository.put(any(SaveRequest.class)))
          .thenReturn(Mono.just(ACTUAL_URL));

      tinyUrlService.convert(request).block();

      verify(tinyUrlGenerator, times(2)).generate(ACTUAL_URL);
    }

    @Test
    public void shouldNotStoreTinyUrlToActualUrlWhenTinyUrlIsAlreadyGenerated() {

      when(tinyUrlGenerator.generate(ACTUAL_URL)).thenReturn(TINY_URL);

      when(repository.putIfAbsent(new SaveRequest(ACTUAL_URL, TINY_URL)))
          .thenReturn(Mono.just(TINY_URL2));

      lenient().when(repository.put(any(SaveRequest.class)))
          .thenReturn(Mono.just(ACTUAL_URL));

      tinyUrlService.convert(request).block();

      verify(repository, never()).put(any(SaveRequest.class));
    }

    @Test
    public void returnSameUrlWhenTinyUrlIsAlreadyGenerated() {

      when(tinyUrlGenerator.generate(ACTUAL_URL))
          .thenReturn(TINY_URL);

      when(repository.putIfAbsent(new SaveRequest(ACTUAL_URL, TINY_URL)))
          .thenReturn(Mono.just(TINY_URL2));

      UrlConversionResponse response = tinyUrlService.convert(request).block();

      assertThat(response).isEqualTo(new UrlConversionResponse(ACTUAL_URL, TINY_URL2));
    }

  }


}