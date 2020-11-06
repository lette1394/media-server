package io.lette1394.mediaserver.storage.infrastructure.springwebflux;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.status;

import io.lette1394.mediaserver.common.ContractViolationException;
import io.lette1394.mediaserver.common.PolicyViolationException;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.BinarySupplierFactory;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectNotFoundException;
import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import io.lette1394.mediaserver.storage.usecase.Copying;
import io.lette1394.mediaserver.storage.usecase.Copying.Command;
import io.lette1394.mediaserver.storage.usecase.Downloading;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class StorageController {
  private final Uploading<DataBufferPayload> uploading;
  private final Downloading<DataBufferPayload> downloading;
  private final Copying<DataBufferPayload> copying;

  @PostMapping(value = "/{area}/{key}", headers = "!from")
  CompletableFuture<? extends ResponseEntity<Void>> putObject(
    @PathVariable String area,
    @PathVariable String key,
    @RequestHeader(value = "Content-length", required = false) Optional<Long> contentLength,
    ServerHttpRequest request) {

    // TODO: 의미있는 response
    final Publisher<DataBufferPayload> body = request
      .getBody()
      .map(DataBufferPayload::new);

    return uploading
      .upload(Uploading.Command.<DataBufferPayload>builder()
        .identifier(new Identifier(area, key))
        .upstream(BinarySupplierFactory.from(body, contentLength))
        .tags(new HashMap<>())
        .build())
      .handle(this::response);
  }

  @PostMapping(value = "/{toArea}/{toKey}", headers = "from")
  CompletableFuture<? extends ResponseEntity<Void>> copyObject(
    @PathVariable String toArea,
    @PathVariable String toKey,
    @RequestHeader("from") String from) {

    final String[] split = from.split("/");

    return copying.copy(Command.builder()
      .from(new Identifier(split[0], split[1]))
      .to(new Identifier(toArea, toKey))
      .build())
      .handle(this::response);
  }

  @GetMapping("/{area}/{key}")
  Mono<Void> getObject(
    @PathVariable String area,
    @PathVariable String key,
    ServerHttpResponse response) {

    final CompletableFuture<Mono<Void>> monoCompletableFuture = downloading
      .download(new Identifier(area, key))
      .thenApply(BinarySupplier::publisher)
      .thenApply(Flux::from)
      .thenApply(flux -> flux.map(DataBufferPayload::getValue))
      .thenApply(response::writeWith);
    return Mono
      .fromFuture(monoCompletableFuture)
      .flatMap(__ -> __);
  }

  private ResponseEntity<Void> response(Object<?> object, Throwable throwable) {
    if (throwable == null) {
      return translateResponse(object);
    }
    return translateException(throwable);
  }

  private ResponseEntity<Void> translateResponse(Object<?> object) {
    return ResponseEntity.ok().build();
  }

  private ResponseEntity<Void> translateException(Throwable throwable) {
    if (throwable == null) {
      return status(500).build();
    }
    if (throwable instanceof CompletionException) {
      return translateException(throwable.getCause());
    }

    return Match(throwable)
      .of(
        Case(
          $(instanceOf(PolicyViolationException.class)),
          () -> badRequest().build()),
        Case(
          $(instanceOf(ObjectNotFoundException.class)),
          () -> notFound().build()),
        Case(
          $(instanceOf(ObjectNotFoundException.class)),
          () -> notFound().build()),
        Case(
          $(instanceOf(ContractViolationException.class)),
          () -> status(INTERNAL_SERVER_ERROR).build()),
        Case(
          $(),
          () -> status(INTERNAL_SERVER_ERROR).build())
      );
  }
}
