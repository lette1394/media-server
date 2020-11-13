package io.lette1394.mediaserver.processing.instrastructure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.lette1394.mediaserver.MemoryLeakTest;
import io.lette1394.mediaserver.processing.domain.DecodedMetadata;
import io.lette1394.mediaserver.processing.domain.MediaDecoder;
import io.lette1394.mediaserver.processing.domain.MediaDecoder.Listener;
import io.lette1394.mediaserver.processing.domain.PayloadParser.DataBufferPayloadParser;
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
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import reactor.core.publisher.Flux;

@Tag("slow")
class DataBufferApacheTikaMediaDecoderTest extends MemoryLeakTest {
  static String imagePath = "/sample_image_3840x2160_537055_bytes.jpg";
  static String videoPath = "/file_example_MP4_480_1_5MG.mp4";

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

  // TODO: Add leak detector
  @Test
  @SneakyThrows
  void imageWithLargeBuffer() {
    subjectWithLargeChunk(imagePath, new Listener() {
      @Override
      public void afterDecoded(DecodedMetadata decodedMetadata) {
        decoded.set(true);
        decodedWidth.set(decodedMetadata.getAsLong("Image Width"));
        decodedHeight.set(decodedMetadata.getAsLong("Image Height"));
      }
    }).join();

    assertThat(decoded.get(), is(true));
    assertThat(decodedWidth.get(), is(3840L));
    assertThat(decodedHeight.get(), is(2160L));
  }

  @Test
  @SneakyThrows
  void imageWithSmallBuffer() {
    subjectWithSmallChunk(imagePath, new Listener() {
      @Override
      public void afterDecoded(DecodedMetadata decodedMetadata) {
        // todo: refactor
        decoded.set(true);
        decodedWidth.set(decodedMetadata.getAsLong("Image Width"));
        decodedHeight.set(decodedMetadata.getAsLong("Image Height"));
      }
    }).join();

    assertThat(decoded.get(), is(true));
    assertThat(decodedWidth.get(), is(3840L));
    assertThat(decodedHeight.get(), is(2160L));
  }

  @Test
  @SneakyThrows
  void videoWithLargeBuffer() {
    subjectWithLargeChunk(videoPath, new Listener() {
      @Override
      public void afterDecoded(DecodedMetadata decodedMetadata) {
        decoded.set(true);
        decodedWidth.set(decodedMetadata.getAsLong("tiff:ImageWidth"));
        decodedHeight.set(decodedMetadata.getAsLong("tiff:ImageLength"));
      }
    }).join();

    assertThat(decoded.get(), is(true));
    assertThat(decodedWidth.get(), is(480L));
    assertThat(decodedHeight.get(), is(270L));
  }

  @Test
  @SneakyThrows
  void videoWithSmallBuffer() {
    subjectWithSmallChunk(videoPath, new Listener() {
      @Override
      public void afterDecoded(DecodedMetadata decodedMetadata) {
        decoded.set(true);
        decodedWidth.set(decodedMetadata.getAsLong("tiff:ImageWidth"));
        decodedHeight.set(decodedMetadata.getAsLong("tiff:ImageLength"));
      }
    }).join();

    assertThat(decoded.get(), is(true));
    assertThat(decodedWidth.get(), is(480L));
    assertThat(decodedHeight.get(), is(270L));
  }

  private CompletableFuture<Void> subjectWithLargeChunk(String path, Listener listener) {
    return subject(path, 1024 * 8, listener);
  }

  private CompletableFuture<Void> subjectWithSmallChunk(String path, Listener listener) {
    return subject(path, 1024, listener);
  }

  private CompletableFuture<Void> subject(String path, int bufferSize, Listener listener) {
    final CompletableFuture<Void> ret = new CompletableFuture<>();
    final MediaDecoder<DataBufferPayload> subject = subject(listener);

    Flux.from(binarySource(path, bufferSize))
      .doOnNext(buffer -> subject.appendNext(buffer))
      .doOnEach(__ -> subject.tryDecode())
      .doOnError(e -> ret.completeExceptionally(e))
      .doFinally(__ -> ret.complete(null))
      .subscribe(payload -> payload.release());

    return ret;
  }

  private MediaDecoder<DataBufferPayload> subject(Listener listener) {
    return new ApacheTikaMediaDecoder<>(
      1024 * 128,
      1024 * 1024,
      new DataBufferPayloadParser(),
      listener);
  }

  private Publisher<DataBufferPayload> binarySource(String path, int bufferSize) {
    return DataBufferUtils.read(
      getPath(path),
      new NettyDataBufferFactory(memoryLeakDetectableByteBufAllocator),
      bufferSize)
      .map(dataBuffer -> new DataBufferPayload(dataBuffer));
  }

  private Listener composite(Listener first, Listener last) {
    return new Listener() {
      @Override
      public void beforeDecodingStarted() {
        first.beforeDecodingStarted();
        last.beforeDecodingStarted();
      }

      @Override
      public void afterDecoded(DecodedMetadata decodedMetadata) {
        first.afterDecoded(decodedMetadata);
        last.afterDecoded(decodedMetadata);
      }

      @Override
      public void afterDecodeFailed(Throwable throwable) {
        first.afterDecodeFailed(throwable);
        last.afterDecodeFailed(throwable);
      }
    };
  }

  @SneakyThrows
  private Path getPath(String path) {
    return Paths.get(getClass().getResource(path).toURI());
  }
}