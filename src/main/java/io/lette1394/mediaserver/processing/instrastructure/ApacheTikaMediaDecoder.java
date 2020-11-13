package io.lette1394.mediaserver.processing.instrastructure;

import io.lette1394.mediaserver.processing.domain.DecodedMetadata;
import io.lette1394.mediaserver.processing.domain.MediaDecoder;
import io.lette1394.mediaserver.processing.domain.PayloadParser;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class ApacheTikaMediaDecoder<B extends Payload> implements MediaDecoder<B> {
  private static final Parser parser = new AutoDetectParser(TikaConfig.getDefaultConfig());

  // TODO: extract class
  private final int limitByteArraySize;
  private final ByteArrayOutputStream holder;

  private final PayloadParser<B> payloadParser;
  private final Listener listener;

  private boolean decoded = false;
  private boolean allBinaryUploaded = false;

  public ApacheTikaMediaDecoder(
    int initialByteArraySize,
    int limitByteArraySize,
    PayloadParser<B> payloadParser,
    Listener listener) {

    this.limitByteArraySize = limitByteArraySize;
    this.holder = new ByteArrayOutputStream(initialByteArraySize);
    this.payloadParser = payloadParser;
    this.listener = listener;
  }

  @Override
  public void appendNext(B payload) {
    if (holder.size() > limitByteArraySize) {
//       TODO: notify to listeners
      return;
    }
    holder.writeBytes(payloadParser.parse(payload));
  }

  @Override
  public void appendCompleted() {
    allBinaryUploaded = true;
  }

  @Override
  public void tryDecode() {
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
        return;
      }

      decoded = true;
      final Map<String, String> decoded = new HashMap<>();
      for (String name : metadata.names()) {
        decoded.put(name, metadata.get(name));
      }
      listener.afterDecoded(new DecodedMetadata(decoded));
    } catch (IOException | SAXException e) {
      listener.afterDecodeFailed(e);
    } catch (TikaException e) {
      if (allBinaryUploaded) {
        listener.afterDecodeFailed(new RuntimeException("cannot decoded"));
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
