package io.lette1394.mediaserver.storage.infrastructure.filesystem;

import static io.lette1394.mediaserver.storage.domain.BinaryPublisher.adapt;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.Context;
import io.lette1394.mediaserver.storage.domain.DelegatingBinaryPublisher;
import io.lette1394.mediaserver.storage.domain.DelegatingSubscriber;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.NoOperationSubscriber;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectNotFoundException;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
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
      if (e instanceof NoSuchFileException) {
        return failedFuture(new ObjectNotFoundException(e));
      }
      return failedFuture(e);
    }
  }

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
  public CompletableFuture<BinaryPublisher<T>> find(BinaryPath binaryPath) {
    try {
      return completedFuture(
        new FileSystemBinaryPublisher<>(readBinary(binaryPath), createPath(binaryPath)));
    } catch (Throwable e) {
      return failedFuture(e);
    }
  }

  @Override
  public CompletableFuture<Void> create(BinaryPath binaryPath, BinaryPublisher<T> binaryPublisher) {
    return writeBinary(binaryPath, binaryPublisher,
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE);
  }

  @Override
  public CompletableFuture<Void> append(BinaryPath binaryPath, BinaryPublisher<T> binaryPublisher) {
    return writeBinary(binaryPath, binaryPublisher, StandardOpenOption.APPEND);
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

  private BinaryPublisher<T> readBinary(BinaryPath binaryPath) {
    return adapt(read(createPath(binaryPath)));
  }

  private CompletableFuture<Void> writeBinary(
    BinaryPath binaryPath,
    BinaryPublisher<T> binaryPublisher,
    OpenOption... openOption) {
    final Path target = createPath(binaryPath);
    final Context context = binaryPublisher.currentContext();

    try {
      ensureParentExists(target);

      // TODO: extract field
      final Path source = context.getOrDefault("filesystem.supplier.source", null);
      if (source == null) {
        return writeFromUpstream(binaryPublisher, target, openOption);
      }
      return writeFromFilesystem(binaryPublisher, target);
    } catch (Exception e) {
      return failedFuture(e);
    }
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void ensureParentExists(Path target) {
    final Path parent = target.getParent();
    if (parent == null) {
      throw new RuntimeException("parent not exists");
    }
    parent.toFile().mkdirs();
  }

  private CompletableFuture<Void> writeFromUpstream(BinaryPublisher<T> binaryPublisher, Path target,
    OpenOption[] openOption) throws IOException {
    final WritableByteChannel channel = Channels
      .newChannel(Files.newOutputStream(target, openOption));
    final CompletableFuture<Void> ret = new CompletableFuture<>();

    Flux.from(binaryPublisher)
      .doOnComplete(() -> ret.complete(null))
      .doOnError(e -> ret.completeExceptionally(e))
      .doFinally(__ -> IOUtils.closeQuietly(channel, null))
      .subscribe(item -> write(channel, item));

    return ret;
  }

  private CompletableFuture<Void> writeFromFilesystem(BinaryPublisher<T> binaryPublisher,
    Path target) {
    final Context context = binaryPublisher.currentContext();
    try {
      final Path source = context.getOrDefault("filesystem.supplier.source", null);
      binaryPublisher.subscribe(NoOperationSubscriber.instance());

      assert source != null;
      final Path copied = Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
      final long size = Files.size(copied);

      final Consumer<Long> completeNormally = context
        .get("filesystem.supplier.completeNormally()");
      completeNormally.accept(size);
      return completedFuture(null);
    } catch (IOException e) {
      final Consumer<Throwable> completeExceptionally = context
        .get("filesystem.supplier.completeExceptionally()");
      completeExceptionally.accept(e);
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

  private static class FileSystemBinaryPublisher<P extends Payload>
    extends DelegatingBinaryPublisher<P> {

    private final Path source;
    private Subscriber<? super P> originalSubscriber;

    public FileSystemBinaryPublisher(BinaryPublisher<P> delegate, Path source) {
      super(delegate);
      this.source = source;
    }

    @Override
    public void subscribe(Subscriber<? super P> s) {
      super.subscribe(new DelegatingSubscriber<P, P>(originalSubscriber) {
        @Override
        public void onSubscribe(Subscription s) {
          FileSystemBinaryPublisher.this.originalSubscriber = delegate;
          delegate.onSubscribe(s);
        }

        @Override
        public void onNext(P payload) {
          delegate.onNext(payload);
        }
      });
    }

    @Override
    public Context currentContext() {
      final Consumer<Long> completeNormally = size -> {
        notifyBinarySize(size);
        originalSubscriber.onComplete();
      };

      final Consumer<Throwable> completeExceptionally
        = throwable -> originalSubscriber.onError(throwable);

      return delegate.currentContext()
        .put("filesystem.supplier.source", source)
        .put("filesystem.supplier.completeNormally()", completeNormally)
        .put("filesystem.supplier.completeExceptionally()", completeExceptionally);
    }

    @SuppressWarnings("unchecked")
    private void notifyBinarySize(Long size) {
      originalSubscriber.onNext((P) (Payload) () -> size);
    }
  }
}
