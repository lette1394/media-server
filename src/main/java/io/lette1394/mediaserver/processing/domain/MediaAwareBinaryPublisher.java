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

// TODO: 이걸 publisher 로 하는게 맞나?
//  객체 생성 시점에 binary publisher 를 알 수가 없는데...
//  이건 필요한 건 맞는거 같은데
//  실제로 usecase에서 사용하는 거는 별도 인터페이스가 필요해보임
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

    // TODO: 1. local은 apache tika 로
    //  2. remote 로는 음.. 구현은 없지만 테스트는 가능하게
    //  리스너는 비동기로 터진다
    //  .
    //  이제 이걸 실제로 어떻게 object tag에 전달하냐 인데...
    //  final



    // TODO: tika 붙여서 일단 video test는 통과하게 만들어두자.
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
