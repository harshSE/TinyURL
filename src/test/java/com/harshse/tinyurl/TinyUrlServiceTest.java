package com.harshse.tinyurl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TinyUrlServiceTest {

    public static final String ACTUAL_URL = "https://www.test.com";
    public static final String TINY_URL = "abc";

    @Mock
    private TinyUrlGenerator tinyUrlGenerator;

    @InjectMocks
    private TinyUrlService tinyUrlService;

    @Test
    public void convertGivenUrlToTinyUrl() {

        UrlConversionRequest request = new UrlConversionRequest(ACTUAL_URL);

        when(tinyUrlGenerator.generate(ACTUAL_URL)).thenReturn(TINY_URL);

        UrlConversionResponse response = tinyUrlService.convert(request).block();

        assertThat(response).isEqualTo(new UrlConversionResponse(ACTUAL_URL, TINY_URL));
    }

    @Test
    public void returnSameUrlWhenTinyUrlIsAlreadyGenerated() {

        UrlConversionRequest request = new UrlConversionRequest(ACTUAL_URL);

        when(tinyUrlGenerator.generate(ACTUAL_URL))
                .thenReturn(TINY_URL)
                .thenThrow(RuntimeException.class);

        UrlConversionResponse response = tinyUrlService.convert(request).block();

        UrlConversionResponse response2 = tinyUrlService
                .convert(new UrlConversionRequest("https://www.test.com"))
                .block();

        assertThat(response).isEqualTo(response2);
    }

}