package io.lette1394.mediaserver.storage.infrastructure.filesystem;

import io.lette1394.mediaserver.storage.infrastructure.ByteBufferPayload;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

public class ByteBufferFileSystemRepository extends FileSystemRepository<ByteBufferPayload> {

  public ByteBufferFileSystemRepository(String baseDir) {
    super(baseDir);
  }

  @Override
  protected Publisher<ByteBufferPayload> toBinaryPublisher(Path path) {
    try {
      final AsynchronousFileChannel channel = AsynchronousFileChannel
        .open(path, StandardOpenOption.READ);


      return DataBufferUtils
        .readAsynchronousFileChannel(() -> channel, new DefaultDataBufferFactory(), 1024)
        .map(buffer -> new ByteBufferPayload(buffer.asByteBuffer()));

    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void write(WritableByteChannel channel, ByteBufferPayload item) {
    try {
      channel.write(item.getValue());
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
