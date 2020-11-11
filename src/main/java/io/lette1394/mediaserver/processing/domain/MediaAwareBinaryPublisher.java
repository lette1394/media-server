package io.lette1394.mediaserver.processing.domain;

import static java.util.Objects.nonNull;

import io.lette1394.mediaserver.common.Contracts;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.DelegatingBinaryPublisher;
import io.lette1394.mediaserver.storage.domain.DelegatingSubscriber;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import lombok.SneakyThrows;
import org.reactivestreams.Publisher;

public abstract class MediaAwareBinaryPublisher<B extends Payload> extends
  DelegatingBinaryPublisher<B> {
  private final Listener listener;

  public MediaAwareBinaryPublisher(BinaryPublisher<B> delegate, Listener listener) {
    super(delegate);
    Contracts.require(nonNull(listener), "nonNull(listener)");
    this.listener = listener;
  }

  @Override
  @SneakyThrows
  public Publisher<B> publisher() {
    final AtomicBoolean decoded = new AtomicBoolean(false);
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final Runnable run = () -> {
      if (decoded.get()) {
        return;
      }
      try {
        final InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        final ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
        final Iterator<ImageReader> it = ImageIO.getImageReaders(imageInputStream);

        while (it.hasNext()) {
          ImageReader reader = it.next();
          reader.setInput(imageInputStream);
          final int width = reader.getWidth(0);
          final int height = reader.getHeight(0);
          listener.decoded(width, height);
          decoded.set(true);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    };

    final AtomicLong read = new AtomicLong(0L);

    return subscriber -> delegate.publisher().subscribe(new DelegatingSubscriber<B>(subscriber) {
      @Override
      @SneakyThrows
      public void onNext(B b) {
        if (decoded.get()) {
          super.onNext(b);
          return;
        }
        outputStream.write(getByte(b));
        read.addAndGet(b.getSize());
        if (read.get() % 1024 == 0) {
          run.run();
        }
        super.onNext(b);
      }

      @Override
      public void onComplete() {
        if (decoded.get()) {
          super.onComplete();
          return;
        }
        run.run();
        super.onComplete();
      }
    });
  }

  protected abstract byte[] getByte(B payload);

  public interface Listener {
    void decoded(int width, int height);
  }
}
