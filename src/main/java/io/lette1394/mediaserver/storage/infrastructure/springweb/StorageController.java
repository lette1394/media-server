package io.lette1394.mediaserver.storage.infrastructure.springweb;

import io.lette1394.mediaserver.storage.domain.binary.BinarySupplierFactory;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.usecase.Downloading;
import io.lette1394.mediaserver.storage.usecase.Downloading.Command;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StorageController {
  private final Downloading downloading;
  private final Uploading uploading;

  @GetMapping("/{area}/{key}")
  CompletableFuture<?> getObject(
    @PathVariable String area,
    @PathVariable String key) {

    return downloading.downloadBinary(Command.builder()
      .identifier(new Identifier(area, key))
      .build())
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
