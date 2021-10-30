package com.harshse.tinyurl;

@FunctionalInterface
public interface TinyUrlGenerator {

    Url generate(Url actualUrl);
}
