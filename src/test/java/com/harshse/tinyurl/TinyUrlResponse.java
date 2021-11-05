package com.harshse.tinyurl;


import java.util.Objects;


@SuppressWarnings("unused")
public class TinyUrlResponse {

  private String actual;
  private String tiny;

  public TinyUrlResponse(String actual, String tiny) {
    this.actual = actual;
    this.tiny = tiny;
  }

  public TinyUrlResponse() {
  }

  public String actual() {
    return actual;
  }

  public void setActual(String actual) {
    this.actual = actual;
  }

  public String tiny() {
    return tiny;
  }

  public void setTiny(String tiny) {
    this.tiny = tiny;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TinyUrlResponse that = (TinyUrlResponse) o;
    return actual.equals(that.actual) && tiny.equals(that.tiny);
  }

  @Override
  public int hashCode() {
    return Objects.hash(actual, tiny);
  }

  @Override
  public String toString() {
    return "TinyUrlResponse{" +
        "actual='" + actual + '\'' +
        ", tiny='" + tiny + '\'' +
        '}';
  }
}
