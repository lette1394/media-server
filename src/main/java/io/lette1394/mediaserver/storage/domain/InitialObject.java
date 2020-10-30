package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.TimeStamp;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import org.reactivestreams.Publisher;

public class InitialObject<BUFFER extends SizeAware> extends Object<BUFFER> {

  @Builder
  public InitialObject(Identifier identifier,
    ObjectPolicy objectPolicy, BinaryPolicy binaryPolicy,
    Tags tags, TimeStamp timeStamp,
    BinarySnapshot binarySnapshot,
    BinaryRepository<BUFFER> binaryRepository) {
    super(identifier, objectPolicy, binaryPolicy, tags, timeStamp, binarySnapshot,
      binaryRepository);
  }

  @Override
  protected CompletableFuture<Void> doUpload(BinarySupplier<BUFFER> binarySupplier) {
    return binaryRepository.create(new BinaryPath(), binarySupplier);
  }

  @Override
  protected Publisher<BUFFER> doDownload() {
    throw new UnsupportedOperationException("cannot download initial object");
  }

  @Override
  public ObjectType getType() {
    return ObjectType.INITIAL;
  }

  @Override
  public long getSize() {
    return 0L;
  }
}
