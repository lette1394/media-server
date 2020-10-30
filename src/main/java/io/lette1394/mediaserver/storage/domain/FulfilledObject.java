package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.PositiveLong;
import io.lette1394.mediaserver.common.TimeStamp;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import org.reactivestreams.Publisher;

public class FulfilledObject<BUFFER extends SizeAware> extends Object<BUFFER> {

  private final PositiveLong size;
  private final Publisher<BUFFER> publisher;

  @Builder
  public FulfilledObject(Identifier identifier,
    ObjectPolicy objectPolicy, BinaryPolicy binaryPolicy,
    Tags tags, TimeStamp timeStamp,
    BinarySnapshot binarySnapshot,
    BinaryRepository<BUFFER> binaryRepository, PositiveLong size,
    Publisher<BUFFER> publisher) {
    super(identifier, objectPolicy, binaryPolicy, tags, timeStamp, binarySnapshot,
      binaryRepository);
    this.size = size;
    this.publisher = publisher;
  }

  @Override
  protected CompletableFuture<Void> doUpload(BinarySupplier<BUFFER> binarySupplier) {
    return null;
  }

  @Override
  protected Publisher<BUFFER> doDownload() {
    return publisher;
  }

  @Override
  public long getSize() {
    return size.get();
  }

  @Override
  public ObjectType getType() {
    return ObjectType.FULFILLED;
  }
}
