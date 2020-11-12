package io.lette1394.mediaserver.processing.instrastructure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.lette1394.mediaserver.MemoryLeakDetectableByteBufAllocator;
import io.lette1394.mediaserver.MemoryLeakTest;
import io.lette1394.mediaserver.processing.domain.MediaAwareBinaryPublisher;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import reactor.core.publisher.Flux;

@Tag("slow")
class DataBufferMediaAwareBinaryPublisherTest extends MemoryLeakTest {

  public static final int TRIGGER_TIMEOUT = 1000;
  static String imagePath = "/sample_image_3840x2160_537055_bytes.jpg";
  static String videoPath = "/sample_video_1280x720_31491130_bytes.mp4";

  AtomicBoolean decoded;
  AtomicLong decodedWidth;
  AtomicLong decodedHeight;

  CountDownLatch latch;

  @Override
  @BeforeEach
  protected void beforeEach() {
    super.beforeEach();
    decoded = new AtomicBoolean(false);
    decodedWidth = new AtomicLong();
    decodedHeight = new AtomicLong();

    latch = new CountDownLatch(1);
  }

  @Test
  @SneakyThrows
  void imageWithLargeBuffer() {
    triggerSubject(imagePath, 2 * 1024 * 1024);

    assertThat(decoded.get(), is(true));
    assertThat(decodedWidth.get(), is(3840L));
    assertThat(decodedHeight.get(), is(2160L));
  }

  @Test
  @SneakyThrows
  void imageWithSmallBuffer() {
    triggerSubject(imagePath, 128);

    assertThat(decoded.get(), is(true));
    assertThat(decodedWidth.get(), is(3840L));
    assertThat(decodedHeight.get(), is(2160L));
  }

  @Test
  @SneakyThrows
  void videoWithLargeBuffer() {
    triggerSubject(videoPath, 2 * 1024 * 1024);

    assertThat(decoded.get(), is(true));
    assertThat(decodedWidth.get(), is(1280L));
    assertThat(decodedHeight.get(), is(720L));
  }

  @Test
  @SneakyThrows
  void videoWithSmallBuffer() {
    triggerSubject(videoPath, 128);

    assertThat(decoded.get(), is(true));
    assertThat(decodedWidth.get(), is(1280L));
    assertThat(decodedHeight.get(), is(720L));
  }

  private void triggerSubject(String binaryPath, int bufferSize) throws InterruptedException {
    subscribe(subject(binarySource(binaryPath, bufferSize)).publisher());
    latch.await(TRIGGER_TIMEOUT, TimeUnit.MILLISECONDS);
  }

  private void subscribe(Publisher<DataBufferPayload> mediaDecoder) {
    Flux.from(mediaDecoder)
      .doFinally(__ -> latch.countDown())
      .subscribe(payload -> payload.release());
  }

  private MediaAwareBinaryPublisher<DataBufferPayload> subject(BinaryPublisher<DataBufferPayload> publisher) {
    return new MediaAwareBinaryPublisher<>(publisher, null);
  }

  private BinaryPublisher<DataBufferPayload> binarySource(String path, int bufferSize) {
    return () -> DataBufferUtils.read(
      getPath(path),
      new NettyDataBufferFactory(memoryLeakDetectableByteBufAllocator),
      bufferSize)
      .map(dataBuffer -> new DataBufferPayload(dataBuffer));
  }

  @SneakyThrows
  private Path getPath(String path) {
    return Paths.get(getClass().getResource(path).toURI());
  }
}