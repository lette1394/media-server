package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.PositiveLong;
import io.lette1394.mediaserver.common.TimeStamp;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import org.reactivestreams.Publisher;

public class PendingObject<BUFFER extends SizeAware> extends Object<BUFFER> {

  private final PositiveLong size;

  @Builder
  public PendingObject(Identifier identifier,
    ObjectPolicy objectPolicy, BinaryPolicy binaryPolicy,
    Tags tags, TimeStamp timeStamp,
    BinarySnapshot binarySnapshot,
    BinaryRepository<BUFFER> binaryRepository, PositiveLong size) {
    super(identifier, objectPolicy, binaryPolicy, tags, timeStamp, binarySnapshot,
      binaryRepository);
    this.size = size;
  }

  @Override
  protected CompletableFuture<Void> doUpload(BinarySupplier<BUFFER> binarySupplier) {
    return null;
  }

  @Override
  protected Publisher<BUFFER> doDownload() {
    return null;
  }

  @Override
  public ObjectType getType() {
    return ObjectType.PENDING;
  }

  @Override
  public long getSize() {
    return size.get();
  }
}
