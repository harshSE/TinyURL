package com.harshse.tinyurl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class TinyUrlConfiguration {

    @Bean
    public TinyUrlGenerator tinyUrlGenerator() {
        return actualUrl -> UUID.randomUUID().toString();
    }

    @Bean
    public TinyUrlService tinyUrlService(TinyUrlGenerator generator) {
        return new TinyUrlService(generator, new TinyUrlRepository());
    }
}
