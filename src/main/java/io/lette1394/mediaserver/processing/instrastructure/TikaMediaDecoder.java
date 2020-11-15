package io.lette1394.mediaserver.processing.instrastructure;

import io.lette1394.mediaserver.processing.domain.DecodedMetadata;
import io.lette1394.mediaserver.processing.domain.MediaDecoder;
import io.lette1394.mediaserver.processing.domain.PayloadParser;
import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import reactor.core.publisher.Flux;

public class TikaMediaDecoder<P extends Payload> implements MediaDecoder<P> {
  private static final Parser parser = new AutoDetectParser(TikaConfig.getDefaultConfig());

  // TODO: extract class
  private final int limitByteArraySize;
  private final ByteArrayOutputStream holder;

  private final PayloadParser<P> payloadParser;

  private boolean decoded = false;
  private boolean allBinaryUploaded = false;

  private boolean triggered = false;

  private CompletableFuture<DecodedMetadata> ret;

  public TikaMediaDecoder(
    int initialByteArraySize,
    int limitByteArraySize,
    PayloadParser<P> payloadParser) {

    this.limitByteArraySize = limitByteArraySize;
    this.holder = new ByteArrayOutputStream(initialByteArraySize);
    this.payloadParser = payloadParser;
  }

  @Override
  public CompletableFuture<DecodedMetadata> decode(BinaryPublisher<P> binaryPublisher) {

    // TODO: triggered로 하지말고, 매번 계산 전용 새로운 객체를 생성해주자(method object!)
    //  이러면 TikaMediaDecoder 클래스 자체는 무상태로 남길 수 있다.
    //  'Task' inner class
    if (triggered) {
      return CompletableFuture.failedFuture(new RuntimeException("cannot reuse media decoder"));
    }
    triggered = true;
    ret = new CompletableFuture<>();

    Flux.from(binaryPublisher)
      .doOnNext(payload -> {
        onNextPayload(payload);
        tryDecode();
      })
      .doOnComplete(() -> {
        allBinaryUploaded = true;
        tryDecode();
      })
      .doOnError(e -> ret.completeExceptionally(e))
      .subscribe();

    return ret;
  }

  private void onNextPayload(P payload) {
    if (holder.size() > limitByteArraySize) {
      payload.release();
      return;
    }
    holder.writeBytes(payloadParser.parse(payload));
  }

  private void tryDecode() {
    if (decoded) {
      return;
    }

    try {
      // TODO: support thread pool
      final ContentHandler handler = new BodyContentHandler();
      final Metadata metadata = new Metadata();
      final ParseContext context = new ParseContext();
      parser.parse(new ByteArrayInputStream(holder.toByteArray()), handler, metadata, context);

      if (!isDecoded(metadata)) {
        if (allBinaryUploaded) {
          ret.completeExceptionally(new RuntimeException("cannot decode"));
        }
        return;
      }

      decoded = true;
      final Map<String, String> decoded = new HashMap<>();
      for (String name : metadata.names()) {
        decoded.put(name, metadata.get(name));
      }

      ret.complete(new DecodedMetadata(decoded));
    } catch (IOException | SAXException e) {
      ret.completeExceptionally(e);
    } catch (TikaException e) {
      if (allBinaryUploaded) {
        ret.completeExceptionally(new RuntimeException("cannot decode binary", e));
      }
    }
  }

  // TODO: extends or add decoded condition
  protected boolean isDecoded(Metadata metadata) {
    if (StringUtils.isNotBlank(metadata.get("tiff:ImageWidth"))) {
      return true;
    }
    if (StringUtils.isNotBlank(metadata.get("Image Width"))) {
      return true;
    }
    return false;
  }
}
