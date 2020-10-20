package io.lette1394.mediaserver.domain.storage.object;

import static io.lette1394.mediaserver.domain.storage.object.Policies.allowIfPassed;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.common.Event;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "identifier", callSuper = false)
public abstract class Object extends AggregateRoot {
  public final Identifier identifier;

  protected final BinaryRepository binaryRepository;

  private final Attributes attributes;

  protected Object(
    Identifier identifier,
    Attributes attributes,
    BinaryRepository binaryRepository,
    List<Event.Listener<Event>> listeners) {

    this.identifier = identifier;
    this.attributes = attributes;
    this.binaryRepository = binaryRepository;

    addListeners(listeners);
  }

  public CompletableFuture<Void> upload(BinarySupplier binarySupplier) {
    uploadingTriggered();

    return
      .thenCompose(__ -> upload0(binarySupplier))
      .thenAccept(__ -> uploaded());
  }

  // TODO: rename
  public abstract CompletableFuture<Void> upload0(BinarySupplier binarySupplier);

  public CompletableFuture<BinarySupplier> download() {
    return objectDownloadPolicy.test(this)
      .thenAccept(allowIfPassed())
      .thenCompose(__ -> binaryRepository.findBinary(this));
  }

  public abstract boolean isInitial();

  public abstract boolean isPending();

  public abstract boolean isFulfilled();

  public long getSize() {
    return attributes.getSize().getValue();
  }

  private void uploadingTriggered() {
    addThenPublish(ObjectEvents.UploadingTriggered
      .uploadingTriggered(
        identifier.getArea(),
        identifier.getKey(),
        attributes.getTags().toMap()));
  }

  private void uploaded() {
    addThenPublish(ObjectEvents.Uploaded
      .uploaded(
        identifier.getArea(),
        identifier.getKey(),
        attributes.getTags().toMap()));
  }
}
