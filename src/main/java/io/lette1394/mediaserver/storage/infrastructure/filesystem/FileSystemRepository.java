package io.lette1394.mediaserver.storage.infrastructure.filesystem;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Context;
import io.lette1394.mediaserver.storage.domain.DelegatingBinarySupplier;
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
  public CompletableFuture<BinarySupplier<T>> find(BinaryPath binaryPath) {
    try {
      return completedFuture(
        new FileSystemBinarySupplier<>(readBinary(binaryPath), createPath(binaryPath)));
    } catch (Throwable e) {
      return failedFuture(e);
    }
  }

  @Override
  public CompletableFuture<Void> create(BinaryPath binaryPath, BinarySupplier<T> binarySupplier) {
    // TODO: file system -> file system copy는 어떻게 처리하나?
    //  이 클래스 안에서 FileSystemBinarySupplier 인터페이스를 추가 (외부에 노출되서는 안 됨)
    //  binary find() 연산에서, 이 인터페이스를 구현한 거 return.
    //  create 연산 마다 FileSystemBinarySupplier 의 인스턴스인지 확인
    //  그 인스턴스인 경우 FilePath를 알아내서, 실제 binary를 읽지 않고 filesystem -> filesystem으로 copy가 이뤄질 수 있도록 함
    //  AS-IS
    //         filesystem -> application -> filesystem
    //  TO-BE
    //         filesystem -> filesystem
    //  .
    //  .
    //  .
    //   oop관점에서 이게 맞는건가?
    //   실제로 ControllableBinarySupplier 등 합성된 interface가 동작을 안할거임.
    //   이걸 강제로 trigger 시킬 수는 없다.
    return writeBinary(binaryPath, binarySupplier,
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE);
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
    final Path target = createPath(binaryPath);
    final Context context = binarySupplier.currentContext();

    try {
      ensureParentExists(target);

      final Path source = context.getOrDefault("filesystem.supplier.source", null);
      if (source == null) {
        return writeFromUpstream(binarySupplier, target, openOption);
      }
      return writeFromFilesystem(binarySupplier, target);
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

  private CompletableFuture<Void> writeFromUpstream(BinarySupplier<T> binarySupplier, Path target,
    OpenOption[] openOption) throws IOException {
    final WritableByteChannel channel = Channels
      .newChannel(Files.newOutputStream(target, openOption));
    final CompletableFuture<Void> ret = new CompletableFuture<>();

    Flux.from(binarySupplier.publisher())
      .doOnComplete(() -> ret.complete(null))
      .doOnError(e -> ret.completeExceptionally(e))
      .doFinally(__ -> IOUtils.closeQuietly(channel, null))
      .subscribe(item -> write(channel, item));

    return ret;
  }

  private CompletableFuture<Void> writeFromFilesystem(BinarySupplier<T> binarySupplier,
    Path target) {
    final Context context = binarySupplier.currentContext();
    try {
      final Path source = context.getOrDefault("filesystem.supplier.source", null);
      binarySupplier.publisher().subscribe(NoOperationSubscriber.instance());

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

  private static class FileSystemBinarySupplier<T extends Payload> extends
    DelegatingBinarySupplier<T> {
    private final Path source;

    private Subscriber<? super T> subscriber;

    public FileSystemBinarySupplier(BinarySupplier<T> delegate, Path source) {
      super(delegate);
      this.source = source;
    }

    @Override
    public Publisher<T> publisher() throws UnsupportedOperationException {
      final Publisher<T> async = delegate.publisher();
      return subscriber -> async
        .subscribe(new DelegatingSubscriber<>(subscriber) {
          @Override
          public void onSubscribe(Subscription s) {
            FileSystemBinarySupplier.this.subscriber = subscriber;
            subscriber.onSubscribe(s);
          }
        });
    }

    @Override
    public Context currentContext() {
      final Consumer<Long> completeNormally = size -> {
        notifyBinarySize(size);
        subscriber.onComplete();
      };

      final Consumer<Throwable> completeExceptionally
        = throwable -> subscriber.onError(throwable);

      return delegate.currentContext()
        .put("filesystem.supplier.source", source)
        .put("filesystem.supplier.completeNormally()", completeNormally)
        .put("filesystem.supplier.completeExceptionally()", completeExceptionally);
    }

    @SuppressWarnings("unchecked")
    private void notifyBinarySize(Long size) {
      subscriber.onNext((T) new Payload() {
        @Override
        public long getSize() {
          return size;
        }

        @Override
        public void release() {

        }
      });
    }
  }
}
