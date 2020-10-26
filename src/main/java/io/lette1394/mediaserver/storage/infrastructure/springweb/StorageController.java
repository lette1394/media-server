package io.lette1394.mediaserver.storage.infrastructure.springweb;

import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplierFactory;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.usecase.DownloadingBinary;
import io.lette1394.mediaserver.storage.usecase.DownloadingChunked;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.core.runtime.transform.StreamingRequestMarshaller;

@RestController
@RequiredArgsConstructor
public class StorageController {
  private final DownloadingBinary downloadingBinary;
  private final DownloadingChunked downloadingChunked;
  private final Uploading uploading;

  @GetMapping("/{area}/{key}")
  StreamingResponseBody getStreamingObject(@PathVariable String area, @PathVariable String key) {
    final CompletableFuture<BinarySupplier> binaries = downloadingChunked
      .download(new Identifier(area, key));

    // TODO: presentation layer
    return outputStream -> {
      WritableByteChannel channel = Channels.newChannel(outputStream);

      binaries.thenAccept(binarySupplier -> {
        if (binarySupplier.isAsyncSupported()) {
          System.out.println("async download");
          Flux.from(binarySupplier.getAsync())
            .subscribe(byteBuffer -> {
              try {
                channel.write(byteBuffer);
              } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
              }
            });
        }
        if (binarySupplier.isSyncSupported()) {
          System.out.println("sync download");
          try {
            IOUtils.copy(binarySupplier.getSync(), outputStream);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
        throw new RuntimeException();
      });
    };
  }

  @GetMapping("/{area}/{key}")
  CompletableFuture<?> getObject(
    @PathVariable String area,
    @PathVariable String key) {

    // TODO: presentation layer
    return downloadingBinary.download(new Identifier(area, key))
      .thenApply(binarySupplier -> {
        final HttpHeaders httpHeaders = new HttpHeaders();
        final long length = binarySupplier.getLength();
        final InputStream sync = binarySupplier.getSync();
        final InputStreamResource inputStreamResource = new InputStreamResource(sync);

        httpHeaders.setContentLength(length);
        return new ResponseEntity<>(inputStreamResource, httpHeaders, HttpStatus.OK);
      });
  }


  @PostMapping("/{area}/{key}")
  CompletableFuture<?> putStreamingObject(
    @PathVariable String area,
    @PathVariable String key,
    HttpServletRequest request) throws IOException {


    final ServletInputStream inputStream = request.getInputStream();
    final int contentLength = request.getContentLength();

    return uploading.upload(Uploading.Command.builder()
      .identifier(new Identifier(area, key))
      .binarySupplier(BinarySupplierFactory.from(inputStream, contentLength))
      .tags(Map.of())
      .build())
      .thenApply(object -> String.format("uploaded! length:[%s]", contentLength));
  }

  @PostMapping("/{area}/{key}")
  CompletableFuture<?> putObject(
    @PathVariable String area,
    @PathVariable String key,
    HttpServletRequest request) throws IOException {

    final ServletInputStream inputStream = request.getInputStream();
    final int contentLength = request.getContentLength();

    return uploading.upload(Uploading.Command.builder()
      .identifier(new Identifier(area, key))
      .binarySupplier(BinarySupplierFactory.from(inputStream, contentLength))
      .tags(Map.of())
      .build())
      .thenApply(object -> String.format("uploaded! length:[%s]", contentLength));
  }
}
