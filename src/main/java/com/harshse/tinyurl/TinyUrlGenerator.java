package com.harshse.tinyurl;

@FunctionalInterface
public interface TinyUrlGenerator {
    String generate(String actualUrl);
}
