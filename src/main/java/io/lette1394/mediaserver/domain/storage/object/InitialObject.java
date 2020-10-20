package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.Event;
import io.lette1394.mediaserver.common.Event.Listener;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;

public class InitialObject extends Object {

  @Builder
  public InitialObject(Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    List<Listener<Event>> listeners) {
    super(identifier, attributes, binaryRepository, listeners);
  }

  @Override
  public CompletableFuture<Void> upload0(BinarySupplier binarySupplier) {
    return binaryRepository.createBinary(this, binarySupplier);
  }

  @Override
  public boolean isInitial() {
    return true;
  }

  @Override
  public boolean isPending() {
    return false;
  }

  @Override
  public boolean isFulfilled() {
    return false;
  }
}
