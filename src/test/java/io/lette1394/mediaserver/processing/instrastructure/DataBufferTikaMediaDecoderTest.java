package io.lette1394.mediaserver.processing.instrastructure;

import static io.lette1394.mediaserver.storage.domain.BinaryPublisher.adapt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.lette1394.mediaserver.MemoryLeakTest;
import io.lette1394.mediaserver.processing.domain.DecodedMetadata;
import io.lette1394.mediaserver.processing.domain.MediaDecoder;
import io.lette1394.mediaserver.processing.domain.PayloadParser.DataBufferPayloadParser;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import reactor.core.publisher.Flux;

@Tag("slow")
class DataBufferTikaMediaDecoderTest extends MemoryLeakTest {
  static String imagePath = "/sample_image_3840x2160_537055_bytes.jpg";
  static String videoPath = "/sample_video_480x270_1570024_bytes.mp4";

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
  void image() {
    final DecodedMetadata metadata = subject(imagePath).join();

    assertThat(metadata.getAsLong("Image Width"), is(3840L));
    assertThat(metadata.getAsLong("Image Height"), is(2160L));
  }

  @Test
  void video() {
    final DecodedMetadata metadata = subject(videoPath).join();

    assertThat(metadata.getAsLong("tiff:ImageWidth"), is(480L));
    assertThat(metadata.getAsLong("tiff:ImageLength"), is(270L));
  }

  private CompletableFuture<DecodedMetadata> subject(String path) {
    return subject(path, 1024 * 1024);
  }

  private CompletableFuture<DecodedMetadata> subject(String path, int bufferSize) {
    final BinaryPublisher<DataBufferPayload> retainedBroadcastingPublisher =
      binarySource(path, bufferSize)
        .broadcast(2);

    Flux.from(retainedBroadcastingPublisher)
      .doOnNext(payload -> payload.release())
      .subscribe();

    return subject().decode(retainedBroadcastingPublisher);
  }

  private MediaDecoder<DataBufferPayload> subject() {
    return new TikaMediaDecoder<>(
      1024 * 1024,
      1024 * 1024 * 50,
      new DataBufferPayloadParser());
  }

  private BinaryPublisher<DataBufferPayload> binarySource(String path, int bufferSize) {
    return adapt(DataBufferUtils.read(
      getPath(path),
      new NettyDataBufferFactory(memoryLeakDetectableByteBufAllocator),
      bufferSize)
      .map(dataBuffer -> new DataBufferPayload(dataBuffer)));
  }

  @SneakyThrows
  private Path getPath(String path) {
    return Paths.get(getClass().getResource(path).toURI());
  }
}