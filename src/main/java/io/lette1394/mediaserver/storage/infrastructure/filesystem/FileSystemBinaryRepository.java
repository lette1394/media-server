package io.lette1394.mediaserver.storage.infrastructure.filesystem;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.storage.infrastructure.SingleThreadInputStreamPublisher;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Identifier;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import lombok.Value;
import org.reactivestreams.Publisher;

@Value
public class FileSystemBinaryRepository implements BinaryRepository {
  String baseDir;

  private static boolean isEmptyDirectory(Path path) throws IOException {
    if (path == null) {
      return false;
    }
    if (Files.isDirectory(path)) {
      try (Stream<Path> entries = Files.list(path)) {
        return entries.findFirst().isEmpty();
      }
    }
    return false;
  }

  @Override
  public CompletableFuture<Result<BinarySupplier>> findBinary(
    Identifier identifier) {
    try {
      return completedFuture(Result.succeed(readOp(identifier)));
    } catch (IOException e) {
      return failedFuture(e);
    }
  }

  @Override
  public CompletableFuture<Result<Void>> createBinary(Identifier identifier,
    BinarySupplier binarySupplier) {
    return writeOp(identifier, binarySupplier, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
      StandardOpenOption.READ)
      .thenApply(aVoid -> Result.succeed());
  }

  @Override
  public CompletableFuture<Result<Void>> appendBinary(Identifier identifier,
    BinarySupplier binarySupplier) {
    return writeOp(identifier, binarySupplier, StandardOpenOption.APPEND)
      .thenApply(aVoid -> Result.succeed());
  }

  @Override
  public CompletableFuture<Result<Void>> deleteBinary(Identifier identifier) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        final Path path = createPath(identifier);
        final Path parent = path.getParent();

        Files.delete(path);
        if (isEmptyDirectory(parent)) {
          parent.toFile().delete();
        }
        return null;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private CompletableFuture<Void> writeOp(Identifier identifier, BinarySupplier binarySupplier,
    OpenOption... openOption) {
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final Path path = createPath(identifier);
    final Path parent = path.getParent();

    try {
      if (parent == null) {
        return failedFuture(new RuntimeException("parent not exists"));
      }
      parent.toFile().mkdirs();
      Files.copy(binarySupplier.getSync(), path, StandardCopyOption.REPLACE_EXISTING);

      return completedFuture(null);
    } catch (IOException e) {
      return failedFuture(e);
    } finally {
      executorService.shutdownNow();
    }
  }

  private BinarySupplier readOp(Identifier identifier) throws IOException {
    final Path objectPath = createPath(identifier);
    final InputStream inputStream = Files.newInputStream(objectPath, StandardOpenOption.READ);

    return new BinarySupplier() {
      @Override
      public boolean isSyncSupported() {
        return true;
      }

      @Override
      public boolean isAsyncSupported() {
        return true;
      }

      @Override
      public long getSize() {
        return objectPath.toFile().length();
      }

      @Override
      public InputStream getSync() {
        return inputStream;
      }

      @Override
      public Publisher<ByteBuffer> getAsync() {
        return new SingleThreadInputStreamPublisher(inputStream, 1024);
      }
    };
  }

  private Path createPath(Identifier identifier) {
    return Paths.get(
      baseDir,
      identifier.getArea(),
      identifier.getKey()).toAbsolutePath();
  }
}
