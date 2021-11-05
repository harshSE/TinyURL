package com.harshse.tinyurl;

import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TinyUrlConfiguration {

    @Bean
    public TinyUrlGenerator tinyUrlGenerator() {
        return actualUrl -> new Url(UUID.randomUUID().toString());
    }

    @Bean
    public TinyUrlService tinyUrlService(TinyUrlGenerator generator, TinyUrlRepository repository) {
        return new TinyUrlService(generator, repository);
    }
}
