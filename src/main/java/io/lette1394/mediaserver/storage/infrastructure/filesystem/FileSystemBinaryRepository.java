package io.lette1394.mediaserver.storage.infrastructure.filesystem;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.domain.object.ObjectRepository;
import io.lette1394.mediaserver.storage.infrastructure.SingleThreadInputStreamPublisher;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import lombok.Value;
import org.reactivestreams.Publisher;

@Value
public class FileSystemBinaryRepository implements ObjectRepository, BinaryRepository {
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

  private static CompletableFuture<Void> wrap(Runnable runnable) {
    try {
      runnable.run();
      return completedFuture(null);
    } catch (Exception e) {
      return failedFuture(e);
    }
  }

  private static <T> CompletableFuture<T> wrap(Callable<T> callable) {
    try {
      return completedFuture(callable.call());
    } catch (Exception e) {
      return failedFuture(e);
    }
  }

  @Override
  public CompletableFuture<Boolean> objectExists(Identifier identifier) {
    return completedFuture(Files.exists(createPath(identifier, ".txt")));
  }

  @Override
  public CompletableFuture<Object> findObject(Identifier identifier) {
    return wrap(() -> {
      final byte[] bytes = Files.readAllBytes(createPath(identifier, ".txt"));
      return FileSystemObjectEntity.fromBytes(bytes, this).getObject();
    });
  }

  @Override
  public CompletableFuture<Object> saveObject(Object object) {
    final byte[] bytes = new FileSystemObjectEntity(object).toBytes();
    return wrap(() -> {
      Files.write(ensureDirectory(createPath(object.getIdentifier(), ".txt")), bytes);
      return object;
    });
  }

  @Override
  public CompletableFuture<Void> deleteObject(Identifier identifier) {
    return wrap(() -> {
      Files.delete(createPath(identifier, ".txt"));
      return null;
    });
  }

  @Override
  public CompletableFuture<BinarySupplier> findBinary(
    Identifier identifier) {
    try {
      return completedFuture(readBinary(identifier));
    } catch (IOException e) {
      return failedFuture(e);
    }
  }

  @Override
  public CompletableFuture<Void> saveBinary(Identifier identifier,
    BinarySupplier binarySupplier) {
    return writeOp(identifier, binarySupplier, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
      StandardOpenOption.READ);
  }

  @Override
  public CompletableFuture<Void> appendBinary(Identifier identifier,
    BinarySupplier binarySupplier) {
    return writeOp(identifier, binarySupplier, StandardOpenOption.APPEND);
  }

  @Override
  public CompletableFuture<Void> deleteBinary(Identifier identifier) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        final Path path = createPath(identifier, "");
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
    final Path path = createPath(identifier, "");
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

  private BinarySupplier readBinary(Identifier identifier) throws IOException {
    final Path objectPath = createPath(identifier, "");
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
      public long getLength() {
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

  private Path createPath(Identifier identifier, String ext) {
    return Paths.get(
      baseDir,
      identifier.getArea(),
      identifier.getKey() + ext).toAbsolutePath();
  }

  private Path ensureDirectory(Path path) throws IOException {
    if (Files.notExists(path.getParent())) {
      Files.createDirectories(path.getParent());
    }
    return path;
  }
}
