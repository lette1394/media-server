package io.lette1394.mediaserver.domain.storage.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.Object;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class StorageFactoryTest {
  Subscription subscription;

  @Test
  @SneakyThrows
  void test1() {

    final byte[] testBinary = {
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
      10, 11, 12, 13, 14, 15, 16, 17, 18, 19
    };
    final AsyncStorage asyncStorage =
        new InMemoryStorage(() -> new ByteArrayInputStream(testBinary), 5);

    final Object object = Object.create("1", "2");
    final Result upload = asyncStorage.upload(object);
    assertThat(upload.isSucceed(), is(true));

    final Object object1 = asyncStorage.find(object.getIdentifier());
    assertThat(object.getIdentifier(), is(equalTo(object1.getIdentifier())));

    final AsyncDataSupplier dataProvider = asyncStorage.downloadAsync(object);

    AtomicInteger atomicInteger = new AtomicInteger();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CountDownLatch latch = new CountDownLatch(1);
    dataProvider
        .getAsync()
        .subscribe(
            new Subscriber<>() {
              @Override
              public void onSubscribe(Subscription subscription) {
                StorageFactoryTest.this.subscription = subscription;
              }

              @Override
              public void onNext(ByteBuffer item) {
                byte[] bytes = new byte[item.remaining()];
                item.get(bytes);
                out.writeBytes(bytes);

                subscription.request(1);
                atomicInteger.incrementAndGet();
              }

              @Override
              public void onError(Throwable throwable) {}

              @Override
              public void onComplete() {
                latch.countDown();
              }
            });

    subscription.request(10);
    latch.await(10, TimeUnit.SECONDS);

    assertThat(out.toByteArray(), is(testBinary));
  }

  @Test
  void test2() {
    final byte[] testBinary = {
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
      10, 11, 12, 13, 14, 15, 16, 17, 18, 19
    };
    final Storage storage = new InMemoryStorage(() -> new ByteArrayInputStream(testBinary), 50);

    final Object object = Object.create("1", "2");
    final Result upload = storage.upload(object);
    assertThat(upload.isSucceed(), is(true));

    final Object object1 = storage.find(object.getIdentifier());
    assertThat(object.getIdentifier(), is(equalTo(object1.getIdentifier())));

    final DataSupplier dataProvider = storage.download(object);
    final InputStream inputStream = dataProvider.get();
    final ByteArrayOutputStream out = new ByteArrayOutputStream();

    try {
      while (true) {
        final int read = inputStream.read();
        if (read == -1) {
          break;
        }
        out.write(read);
      }
    } catch (IOException e) {
      // ignore
    }

    final byte[] bytes = out.toString().getBytes();
    assertThat(bytes, is(testBinary));
  }
}
