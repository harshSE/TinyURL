package com.harshse.tinyurl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TinyURLController {


  private final TinyUrlService service;
  private final Url host;

  @Autowired
  public TinyURLController(TinyUrlService service, @Value("${tinyurl.host}") Url host) {
    this.service = service;
    this.host = host;
  }


  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<CreateUrlResponse> createTinyUrl(@RequestBody String actualUrl) {

    UrlConversionRequest request = new UrlConversionRequest(new Url(actualUrl));
    Mono<UrlConversionResponse> response = service.convert(request);

    return response.map(
        res -> new CreateUrlResponse(res.actualUlr().value(), host.value() + "/" + res.tinyUrl().value()));
  }

  @GetMapping("/{tinyUrl}")
  @ResponseStatus(HttpStatus.MULTIPLE_CHOICES)
  public Mono<ResponseEntity<Void>> getActualUrl(@PathVariable String tinyUrl) {

    return service.deConvert(new UrlConversionRequest(new Url(tinyUrl)))
        .map(deConversionResponse -> ResponseEntity.
            status(HttpStatus.FOUND).
            header(HttpHeaders.LOCATION, deConversionResponse.actualUlr().value())
            .build());
  }
}
