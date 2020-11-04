package io.lette1394.mediaserver.storage.infrastructure.springwebflux;

import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import io.lette1394.mediaserver.storage.usecase.Copying;
import io.lette1394.mediaserver.storage.usecase.Copying.Command;
import io.lette1394.mediaserver.storage.usecase.Downloading;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
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
  CompletableFuture<?> putObject(
    @PathVariable String area,
    @PathVariable String key,
    ServerHttpRequest request) {
    final Publisher<DataBufferPayload> body = request
      .getBody()
      .map(DataBufferPayload::new);

    return uploading.upload(Uploading.Command.<DataBufferPayload>builder()
      .identifier(new Identifier(area, key))
      .upstream(() -> body)
      .tags(new HashMap<>())
      .build())
      .thenAccept(__ -> System.out.println("done flux"));
  }

  @PostMapping(value = "/{toArea}/{toKey}", headers = "from")
  CompletableFuture<?> copyObject(
    @PathVariable String toArea,
    @PathVariable String toKey,
    @RequestHeader("from") String from) {

    final String[] split = from.split("/");

    return copying.copy(Command.builder()
      .from(new Identifier(split[0], split[1]))
      .to(new Identifier(toArea, toKey))
      .build())
      .thenAccept(__ -> System.out.println("copy"));
  }

  @GetMapping("/{area}/{key}")
  Mono<Void> getObject(
    @PathVariable String area,
    @PathVariable String key,
    ServerHttpResponse response) {

    final CompletableFuture<Mono<Void>> monoCompletableFuture = downloading
      .download(new Identifier(area, key))
      .thenApply(BinarySupplier::getAsync)
      .thenApply(Flux::from)
      .thenApply(flux -> flux.map(DataBufferPayload::getValue))
      .thenApply(response::writeWith);
    return Mono
      .fromFuture(monoCompletableFuture)
      .flatMap(__ -> __);
  }
}