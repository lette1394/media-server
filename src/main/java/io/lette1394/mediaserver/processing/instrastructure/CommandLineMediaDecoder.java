package io.lette1394.mediaserver.processing.instrastructure;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.lette1394.mediaserver.processing.domain.DecodedMetadata;
import io.lette1394.mediaserver.processing.domain.MediaDecoder;
import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.Payload;
import io.lette1394.mediaserver.storage.infrastructure.filesystem.FileSystemRepository;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

@RequiredArgsConstructor
public class CommandLineMediaDecoder<P extends Payload> implements MediaDecoder<P> {
  // TODO: jvm option
  private final static String decodeCommand = "/usr/local/bin/mediainfo";
  private final FileSystemRepository<P> fileSystem;

  private final ExecutorService decoderExecutors = Executors.newFixedThreadPool(10);

  @Override
  public CompletableFuture<DecodedMetadata> decode(BinaryPublisher<P> binaryPublisher) {
    final BinaryPath binaryPath = randomPath();
    return fileSystem
      .create(binaryPath, binaryPublisher)
      .thenComposeAsync(__ -> decode(binaryPath), decoderExecutors)
      .handle((decodedMetadata, e) -> {
        decoderExecutors.submit(() -> fileSystem.delete(binaryPath).join());

        if (Objects.isNull(e)) {
          return decodedMetadata;
        }
        throw new CompletionException(e);
      });
  }

  private BinaryPath randomPath() {
    final String randomString = RandomStringUtils.randomAlphanumeric(100);
    return () -> randomString;
  }

  private CompletableFuture<DecodedMetadata> decode(BinaryPath binaryPath) {
    try {
      final Path path = Paths.get(fileSystem.getBaseDir(), binaryPath.asString());

      final ProcessBuilder builder = new ProcessBuilder()
        .command(decodeCommand, "--Output=JSON", path.toAbsolutePath().toString());

      final Process process = builder.start();
      final boolean done = process.waitFor(1000, TimeUnit.MILLISECONDS);
      if (done && process.exitValue() == 0) {
        final String rawResult = String.join("", IOUtils
          .readLines(process.getInputStream(), Charset.defaultCharset()));
        return CompletableFuture.completedFuture(parse(rawResult));
      }
      throw new RuntimeException("media decoding timeout");

    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private DecodedMetadata parse(String rawResult) {
    final DocumentContext jsonObject = JsonPath.parse(rawResult);

//    final JsonObject jsonObject = new Gson().fromJson(rawResult, JsonObject.class);
    final String type = jsonObject.read("media.track[1].@type", String.class).toLowerCase();
    final String duration = jsonObject.read("media.track[1].Format", String.class).toLowerCase();
    final String width = jsonObject.read("media.track[1].Width", String.class).toLowerCase();
    final String height = jsonObject.read("media.track[1].Height", String.class).toLowerCase();

    final Map<String, String> data = new HashMap<>();
    data.put("type", type);
    data.put("format", duration);
    data.put("width", width);
    data.put("height", height);

    return new DecodedMetadata(data);
  }

  @Data
  private static class MediaInfo {
    String media;
  }
}
