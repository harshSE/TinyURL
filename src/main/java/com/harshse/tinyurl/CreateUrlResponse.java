package com.harshse.tinyurl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CreateUrlResponse {

  @JsonProperty("actual")
  private String actual;

  @JsonProperty("tiny")
  private String tiny;

  public CreateUrlResponse(String actual, String tiny) {
    this.actual = actual;
    this.tiny = tiny;
  }

  public CreateUrlResponse() {
  }


}
