package io.lette1394.mediaserver.storage.infrastructure.filesystem;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public abstract class FileSystemRepository<T extends Payload> implements
  ObjectRepository<T>,
  BinaryRepository<T> {

  private final String baseDir;

  @Override
  public CompletableFuture<Boolean> exists(Identifier identifier) {
    return completedFuture(Files.exists(createPath(identifier)));
  }

  @Override
  public CompletableFuture<Object<T>> find(Identifier identifier) {
    return wrap(() -> {
      final byte[] objectBytes = Files.readAllBytes(createPath(identifier));
      return FileSystemObjectEntity.fromBytes(objectBytes, this).getObject();
    });
  }

  @Override
  public CompletableFuture<Object<T>> save(Object<T> object) {
    final byte[] bytes = new FileSystemObjectEntity<>(object).toBytes();
    return wrap(() -> {
      Files.write(ensureDirectory(createPath(object.getIdentifier())), bytes);
      return object;
    });
  }

  @Override
  public CompletableFuture<Void> delete(Identifier identifier) {
    return wrap(() -> {
      Files.delete(createPath(identifier));
      return null;
    });
  }

  @Override
  public CompletableFuture<BinarySupplier<T>> find(BinaryPath binaryPath) {
    try {
      return completedFuture(readBinary(binaryPath));
    } catch (Throwable e) {
      return failedFuture(e);
    }
  }

  @Override
  public CompletableFuture<Void> create(BinaryPath binaryPath, BinarySupplier<T> binarySupplier) {
    return writeBinary(binaryPath, binarySupplier,
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE,
      StandardOpenOption.READ);
  }

  @Override
  public CompletableFuture<Void> append(BinaryPath binaryPath, BinarySupplier<T> binarySupplier) {
    return writeBinary(binaryPath, binarySupplier, StandardOpenOption.APPEND);
  }

  @Override
  public CompletableFuture<Void> delete(
    BinaryPath binaryPath) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        final Path path = createPath(binaryPath);
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

  protected abstract Publisher<T> read(Path path);

  protected abstract void write(WritableByteChannel channel, T item);

  private BinarySupplier<T> readBinary(BinaryPath binaryPath) {
    return () -> read(createPath(binaryPath));
  }

  private CompletableFuture<Void> writeBinary(
    BinaryPath binaryPath,
    BinarySupplier<T> binarySupplier,
    OpenOption... openOption) {
    final Path path = createPath(binaryPath);
    final Path parent = path.getParent();

    try {
      if (parent == null) {
        return failedFuture(new RuntimeException("parent not exists"));
      }
      parent.toFile().mkdirs();

      final WritableByteChannel channel = Channels
        .newChannel(Files.newOutputStream(path, openOption));
      final CompletableFuture<Void> ret = new CompletableFuture<>();

      Flux.from(binarySupplier.getAsync())
        .doOnComplete(() -> ret.complete(null))
        .doOnError(e -> ret.completeExceptionally(e))
        .subscribe(item -> write(channel, item));

      return ret;
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }
  }

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

  private static <T> CompletableFuture<T> wrap(Callable<T> callable) {
    try {
      return completedFuture(callable.call());
    } catch (Exception e) {
      return failedFuture(e);
    }
  }

  private Path createPath(BinaryPath binaryPath) {
    return Paths.get(
      baseDir,
      binaryPath.asString()).toAbsolutePath();
  }

  private Path createPath(Identifier identifier) {
    return Paths.get(
      baseDir,
      identifier.getArea(),
      identifier.getKey() + ".txt").toAbsolutePath();
  }

  private Path ensureDirectory(Path path) throws IOException {
    if (Files.notExists(path.getParent())) {
      Files.createDirectories(path.getParent());
    }
    return path;
  }
}
