package io.lette1394.mediaserver.storage.infrastructure.springwebflux;

import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import io.lette1394.mediaserver.storage.usecase.Uploading.Command;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StorageController {
  private final Uploading<DataBufferPayload> uploading;

  @PostMapping("/{area}/{key}")
  CompletableFuture<?> putObject(
    @PathVariable String area,
    @PathVariable String key,
    ServerHttpRequest request) {
    final Publisher<DataBufferPayload> body = request
      .getBody()
      .map(DataBufferPayload::new);

    return uploading.upload(Command.<DataBufferPayload>builder()
      .identifier(new Identifier(area, key))
      .upstream(body)
      .tags(new HashMap<>())
      .build())
      .thenAccept(__ -> System.out.println("done flux"));
  }
}
