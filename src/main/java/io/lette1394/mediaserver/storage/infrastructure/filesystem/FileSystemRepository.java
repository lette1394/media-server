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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public abstract class FileSystemRepository<T extends Payload> implements
  ObjectRepository<T>,
  BinaryRepository<T> {

  private final String baseDir;

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

  @Override
  public CompletableFuture<Boolean> objectExists(Identifier identifier) {
    return completedFuture(Files.exists(createPath(identifier, ".txt")));
  }

  @Override
  public CompletableFuture<Object<T>> findObject(Identifier identifier) {
    return wrap(() -> {
      final byte[] objectBytes = Files.readAllBytes(createPath(identifier, ".meta.txt"));
      return FileSystemObjectEntity.fromBytes(objectBytes, this).getObject();
    });
  }

  protected abstract Publisher<T> toBinaryPublisher(Path path);

  @Override
  public CompletableFuture<Object<T>> saveObject(Object<T> object) {
    final byte[] bytes = new FileSystemObjectEntity<>(object).toBytes();
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
  public CompletableFuture<BinarySupplier<T>> findBinary(Identifier identifier) {
    try {
      return completedFuture(readBinary(identifier));
    } catch (IOException e) {
      return failedFuture(e);
    }
  }

  @Override
  public CompletableFuture<Void> saveBinary(Identifier identifier,
    BinarySupplier<T> binarySupplier) {
    return null;
  }

  @Override
  public CompletableFuture<Void> create(BinaryPath binaryPath,
    BinarySupplier<T> binarySupplier) {
    return writeOp(binaryPath, binarySupplier, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
      StandardOpenOption.READ);
  }

  @Override
  public CompletableFuture<Void> appendBinary(Identifier identifier,
    BinarySupplier<T> binarySupplier) {
//    return writeOp(identifier, binarySupplier, StandardOpenOption.APPEND);
    return null;
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

  private CompletableFuture<Void> writeOp(BinaryPath binaryPath,
    BinarySupplier<T> binarySupplier,
    OpenOption... openOption) {
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final Path path = createPath(binaryPath, "");
    final Path parent = path.getParent();

    try {
      if (parent == null) {
        return failedFuture(new RuntimeException("parent not exists"));
      }
      parent.toFile().mkdirs();

      final WritableByteChannel channel = Channels
        .newChannel(Files.newOutputStream(path));
      // TODO: impl
      CompletableFuture<Void> ret = new CompletableFuture<>();

      Flux.from(binarySupplier.getAsync())
        .doOnComplete(() -> ret.complete(null))
        .doOnError(e -> ret.completeExceptionally(e))
        .subscribe(item -> write(channel, item));
      return ret;
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  protected abstract void write(WritableByteChannel channel, T item);

  private BinarySupplier<T> readBinary(Identifier identifier)
    throws IOException {
    final Path binaryPath = createPath(identifier, "");
    return () -> toBinaryPublisher(binaryPath);
  }

  private Path createPath(BinaryPath binaryPath, String ext) {
    return Paths.get(
      baseDir,
      binaryPath.asString() + ext).toAbsolutePath();
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
