package io.lette1394.mediaserver.domain.storage.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.Object;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class StorageFactoryTest {
  Subscription subscription;

  @Test
  @SneakyThrows
  void test1() {
    final AsyncStorage asyncStorage = StorageFactory.create();

    final Object object = Object.create("1", "2");
    final Result upload = asyncStorage.upload(object);
    assertThat(upload.isSucceed(), is(true));

    final Object object1 = asyncStorage.find(object.getIdentifier());
    assertThat(object.getIdentifier(), is(equalTo(object1.getIdentifier())));

    final AsyncDataProvider dataProvider = asyncStorage.downloadAsync(object);

    CountDownLatch latch = new CountDownLatch(1);
    dataProvider.subscribe(
        new Subscriber<ByteBuffer>() {
          @Override
          public void onSubscribe(Subscription subscription) {
            System.out.println("start!!" + subscription.toString());
            StorageFactoryTest.this.subscription = subscription;
          }

          @Override
          public void onNext(ByteBuffer item) {
            final byte b = item.get();
            System.out.println("on next!! " + b);
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("error!!" + throwable);
          }

          @Override
          public void onComplete() {
            System.out.println("completed!!");
            latch.countDown();
          }
        });

    subscription.request(1);
    latch.await(1, TimeUnit.SECONDS);
  }
}
