package io.lette1394.mediaserver.storage.infrastructure.springweb;

import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.BinarySupplierFactory;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.infrastructure.ByteBufferPayload;
import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import io.lette1394.mediaserver.storage.infrastructure.SingleThreadInputStreamPublisher;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import io.lette1394.mediaserver.storage.usecase.Uploading.Command;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class StorageController {

  //  private final DownloadingBinary downloadingBinary;
//  private final DownloadingChunked downloadingChunked;
  private final Uploading<ByteBufferPayload> uploading;

//  @GetMapping("/{area}/{key}")
//  StreamingResponseBody getObject(@PathVariable String area, @PathVariable String key) {
//
//
//    // TODO: presentation layer
//    return outputStream -> {
//      WritableByteChannel channel = Channels.newChannel(outputStream);
//
//      binaries.thenAccept(binarySupplier -> {
//        System.out.println("async download");
//        Flux.from(binarySupplier.getAsync())
//          .subscribe(byteBuffer -> {
//            try {
//              channel.write(byteBuffer);
//            } catch (IOException e) {
//              e.printStackTrace();
//              throw new RuntimeException(e);
//            }
//          });
//      });
//    };
//    return null;
//  }

  @PostMapping("/{area}/{key}")
  CompletableFuture<?> putObject(
    @PathVariable String area,
    @PathVariable String key,
    HttpServletRequest request) throws IOException {
    final ServletInputStream inputStream = request.getInputStream();

    return uploading.upload(Command.<ByteBuffer, ByteBufferPayload>builder()
      .identifier(new Identifier(area, key))
      .upstream(new SingleThreadInputStreamPublisher(inputStream, 10))
      .mapper(byteBuffer -> new ByteBufferPayload(byteBuffer))
      .tags(new HashMap<>())
      .build())
      .thenAccept(__ -> System.out.println("done"));

//
//    final ServletInputStream inputStream = request.getInputStream();
//    final int contentLength = request.getContentLength();
//
//    return uploading.upload(Uploading.Command.builder()
//      .identifier(new Identifier(area, key))
//      .binarySupplier(BinarySupplierFactory.from(new SingleThreadInputStreamPublisher(inputStream, 10), contentLength))
//      .tags(Map.of())
//      .build())
//      .thenApply(object -> String.format("uploaded! length:[%s]", contentLength));
  }
}
