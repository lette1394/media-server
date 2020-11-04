package io.lette1394.mediaserver.storage.infrastructure.springmvc;

import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import io.lette1394.mediaserver.storage.usecase.Uploading.Command;
import io.netty.buffer.PooledByteBufAllocator;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class StorageController {

  private final Uploading<DataBufferPayload> uploading;

  @PostMapping("/{area}/{key}")
  CompletableFuture<?> putObject(
    @PathVariable String area,
    @PathVariable String key,
    HttpServletRequest request) {
    final Flux<DataBufferPayload> body = DataBufferUtils
      .readInputStream(request::getInputStream, new NettyDataBufferFactory(
        new PooledByteBufAllocator(false)), 1024 * 1024)
      .map(DataBufferPayload::new);

    return uploading.upload(Command.<DataBufferPayload>builder()
      .identifier(new Identifier(area, key))
      .upstream(() -> body)
      .tags(new HashMap<>())
      .build())
      .thenAccept(__ -> System.out.println("done mvc"));
  }
}
