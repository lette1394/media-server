package io.lette1394.mediaserver.storage;

import static io.lette1394.mediaserver.common.Contracts.require;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

import io.lette1394.mediaserver.storage.domain.binary.LengthAwareBinarySupplier;
import java.nio.ByteBuffer;
import lombok.Value;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Value
public class BrokenBinarySupplier implements LengthAwareBinarySupplier {

  LengthAwareBinarySupplier delegate;
  long exceptionAt;

  public BrokenBinarySupplier(LengthAwareBinarySupplier delegate, long exceptionAt) {
    require(nonNull(delegate), "require: nonNull(binarySupplier)");
    require(exceptionAt >= 0, "require: exceptionAt >= 0");
    require(delegate.getLength() > exceptionAt, "require: delegate.getSize() > exceptionAt");

    this.delegate = delegate;
    this.exceptionAt = exceptionAt;
  }

  @Override
  public long getLength() {
    return delegate.getLength();
  }

  @Override
  public Publisher<ByteBuffer> getAsync() {
    final Publisher<ByteBuffer> async = delegate.getAsync();
    return new Publisher<>() {
      private long position = 0;

      @Override
      public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
        async.subscribe(new Subscriber<>() {
          @Override
          public void onSubscribe(Subscription s) {
            subscriber.onSubscribe(s);
          }

          @Override
          public void onNext(ByteBuffer byteBuffer) {
            if (position >= exceptionAt) {
              onError(new BrokenIOException(
                format("broken read triggered, size:[%s], exceptionAt:[%s]",
                  getLength(),
                  exceptionAt)));
            } else {
              position += byteBuffer.remaining();
              subscriber.onNext(byteBuffer);
            }
          }

          @Override
          public void onError(Throwable t) {
            subscriber.onError(t);
          }

          @Override
          public void onComplete() {
            subscriber.onComplete();
          }
        });
      }
    };
  }
}
