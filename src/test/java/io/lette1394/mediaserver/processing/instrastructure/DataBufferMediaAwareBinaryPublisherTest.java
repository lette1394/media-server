package io.lette1394.mediaserver.processing.instrastructure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

import io.lette1394.mediaserver.TestByteBufAllocator;
import io.lette1394.mediaserver.processing.domain.MediaAwareBinaryPublisher.Listener;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import reactor.core.publisher.Flux;

class DataBufferMediaAwareBinaryPublisherTest {
  public static final int TRIGGER_TIMEOUT = 1000;
  static String imagePath = "/sample_image_3840x2160_537055_bytes.jpg";
  static String videoPath = "/sample_video_1280x720_31491130_bytes.mp4";

  AtomicBoolean decoded;
  AtomicLong decodedWidth;
  AtomicLong decodedHeight;

  CountDownLatch latch;

  @BeforeEach
  void beforeEach() {
    decoded = new AtomicBoolean(false);
    decodedWidth = new AtomicLong();
    decodedHeight = new AtomicLong();

    latch = new CountDownLatch(1);
  }

  @Test
  @SneakyThrows
  void imageWithLargeBuffer() {
    ensureLeakDetectionEnabled();

    triggerSubject(imagePath, 2 * 1024 * 1024);

    assertThat(decoded.get(), is(true));
    assertThat(decodedWidth.get(), is(3840L));
    assertThat(decodedHeight.get(), is(2160L));
    assertNoLeaks();
  }

  @Test
  @SneakyThrows
  void imageWithSmallBuffer() {
    ensureLeakDetectionEnabled();

    triggerSubject(imagePath, 128);

    assertThat(decoded.get(), is(true));
    assertThat(decodedWidth.get(), is(3840L));
    assertThat(decodedHeight.get(), is(2160L));
    assertNoLeaks();
  }

  @Test
  @SneakyThrows
  void videoWithLargeBuffer() {
    ensureLeakDetectionEnabled();

    triggerSubject(videoPath, 2 * 1024 * 1024);

    assertThat(decoded.get(), is(true));
    assertThat(decodedWidth.get(), is(1280L));
    assertThat(decodedHeight.get(), is(720L));
    assertNoLeaks();
  }

  @Test
  @SneakyThrows
  void videoWithSmallBuffer() {
    ensureLeakDetectionEnabled();

    triggerSubject(videoPath, 128);

    assertThat(decoded.get(), is(true));
    assertThat(decodedWidth.get(), is(1280L));
    assertThat(decodedHeight.get(), is(720L));
    assertNoLeaks();
  }

  private void ensureLeakDetectionEnabled() {
    final String leakDetectionLevel = System.getProperty("io.netty.leakDetection.level");
    if (StringUtils.isBlank(leakDetectionLevel) ||
      !StringUtils.equalsIgnoreCase(leakDetectionLevel, "PARANOID")) {
      fail("need jvm option: -Dio.netty.leakDetection.level=PARANOID");
    }
  }

  private void assertNoLeaks() {
    TestByteBufAllocator.checkForLeaks();
  }

  private void triggerSubject(String binaryPath, int bufferSize) throws InterruptedException {
    subscribe(createMediaAwarePublisher(binarySource(binaryPath, bufferSize)));
    latch.await(TRIGGER_TIMEOUT, TimeUnit.MILLISECONDS);
  }

  private void subscribe(Publisher<DataBufferPayload> mediaDecoder) {
    Flux.from(mediaDecoder)
      .doFinally(__ -> latch.countDown())
      .subscribe(payload -> payload.release());
  }

  private Publisher<DataBufferPayload> createMediaAwarePublisher(
    BinaryPublisher<DataBufferPayload> publisher) {
    return new DataBufferMediaAwareBinaryPublisher(publisher, listener()).publisher();
  }

  private Listener listener() {
    return (width, height) -> {
      decoded.set(true);
      decodedWidth.set(width);
      decodedHeight.set(height);
    };
  }

  private BinaryPublisher<DataBufferPayload> binarySource(String path, int bufferSize) {
    return () -> DataBufferUtils.read(
      getPath(path),
      new NettyDataBufferFactory(TestByteBufAllocator.getInstance()),
      bufferSize)
      .map(dataBuffer -> new DataBufferPayload(dataBuffer));
  }

  @SneakyThrows
  private Path getPath(String path) {
    return Paths.get(getClass().getResource(path).toURI());
  }
}