package io.lette1394.mediaserver.storage.infrastructure.spring.webflux;

import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.BinarySupplierFactory;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import io.lette1394.mediaserver.storage.infrastructure.spring.Translator;
import io.lette1394.mediaserver.storage.usecase.copy.Copying;
import io.lette1394.mediaserver.storage.usecase.copy.Copying.Command;
import io.lette1394.mediaserver.storage.usecase.Downloading;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import io.lette1394.mediaserver.storage.usecase.copy.Copying.CopyMode;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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

  private final Translator translator;

  @PostMapping(value = "/{area}/{key}", headers = "!from")
  CompletableFuture<? extends ResponseEntity<Void>> putObject(
    @PathVariable String area,
    @PathVariable String key,
    @RequestHeader(value = "Content-Length", required = false) Optional<Long> contentLength,
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
      .handle(translator::translate);
  }

  @PostMapping(value = "/{toArea}/{toKey}", headers = "from")
  CompletableFuture<? extends ResponseEntity<Void>> copyObject(
    @PathVariable String toArea,
    @PathVariable String toKey,
    @RequestHeader("from") String from,
    @RequestHeader("mode") String mode) {

    final String[] split = from.split("/");

    return copying
      .copy(Command.builder()
        .from(new Identifier(split[0], split[1]))
        .to(new Identifier(toArea, toKey))
        .mode(CopyMode.valueOf(mode.toUpperCase()))
        .replicatingThreshold(3)
        .build())
      .handle(translator::translate);
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
      .thenApply(flux -> flux.map(DataBufferPayload::getValue))  // TODO: my publisher map()
      .thenApply(response::writeWith);
    return Mono
      .fromFuture(monoCompletableFuture)
      .flatMap(__ -> __)
      .doOnError(throwable -> throwable.printStackTrace());
  }
}
