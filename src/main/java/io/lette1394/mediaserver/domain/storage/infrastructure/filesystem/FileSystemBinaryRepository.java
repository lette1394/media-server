package io.lette1394.mediaserver.domain.storage.infrastructure.filesystem;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import io.lette1394.mediaserver.domain.storage.infrastructure.SingleThreadInputStreamPublisher;
import io.lette1394.mediaserver.domain.storage.object.BinaryRepository;
import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.usecase.AsyncReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.atomic.LongAdder;
import lombok.Value;

@Value
public class FileSystemBinaryRepository implements BinaryRepository {
  String baseDir;

  @Override
  public CompletableFuture<BinarySupplier> findBinary(Object object) {
    try {
      return completedFuture(readOp(object));
    } catch (IOException e) {
      return failedFuture(e);
    }
  }

  @Override
  public CompletableFuture<Void> createBinary(Object object, BinarySupplier binarySupplier) {
    return writeOp(object, binarySupplier, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
  }

  @Override
  public CompletableFuture<Void> appendBinary(Object object, BinarySupplier binarySupplier) {
    return writeOp(object, binarySupplier, StandardOpenOption.APPEND);
  }

  private CompletableFuture<Void> writeOp(Object object, BinarySupplier binarySupplier, OpenOption... openOption) {
    try {
      final Path path = createPath(object);
      path.getParent().toFile().mkdirs();

      final AsynchronousFileChannel channel = AsynchronousFileChannel.open(
        path,
        Set.of(openOption),
        Executors.newSingleThreadExecutor());

      final LongAdder length = new LongAdder();
      final AsyncReader<ByteBuffer, Void> asyncReader = new AsyncReader<>(100) {
        @Override
        protected void translateNext(ByteBuffer item) {
          final int remaining = item.remaining();
          channel.write(item, length.longValue());
          length.add(remaining);
        }

        @Override
        protected Void translateCompleted() {
          return null;
        }
      };

      return asyncReader.read(binarySupplier.getAsync());
    } catch (IOException e) {
      return failedFuture(e);
    }
  }

  private BinarySupplier readOp(Object object) throws IOException {
    final Path objectPath = createPath(object);
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
      public InputStream getSync() {
        return inputStream;
      }

      @Override
      public Publisher<ByteBuffer> getAsync() {
        return new SingleThreadInputStreamPublisher(inputStream, 1024);
      }
    };
  }

  private Path createPath(Object object) {
    return Paths.get(
      baseDir,
      object.identifier.getArea().getValue(),
      object.identifier.getKey().getValue()).toAbsolutePath();
  }
}
